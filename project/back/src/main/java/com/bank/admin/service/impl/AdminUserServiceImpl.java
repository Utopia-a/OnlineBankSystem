package com.bank.admin.service.impl;

import com.bank.admin.dto.request.CreateAdminRequest;
import com.bank.admin.dto.request.UpdateUserStatusRequest;
import com.bank.admin.dto.request.UserQueryRequest;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.UserVO;
import com.bank.admin.repository.AdminUserRepository;
import com.bank.admin.service.AdminUserService;
import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.repository.AccountRepository;
import com.bank.admin.support.AdminAuditHelper;
import com.bank.transaction.exception.BusinessException;
import com.banking.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditHelper adminAuditHelper;

    private static final String CHAR_POOL = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$!";

    @Override
    public PageResult<UserVO> listUsers(UserQueryRequest request) {
        Page<User> page = adminUserRepository.searchUsers(
                request.getKeyword(),
                request.getStatus(),
                request.getRole(),
                request.toSpringPageRequest(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PageResult.of(page.map(UserVO::fromEntity));
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(400, "用户不存在，ID: " + userId));
        return UserVO.fromEntity(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(400, "用户不存在，ID: " + userId));

        if (user.getRole() == User.Role.ROLE_ADMIN) {
            throw new BusinessException(400, "不能修改管理员账号状态");
        }

        User.UserStatus oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        if (request.getStatus() == User.UserStatus.ACTIVE) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
        adminUserRepository.save(user);

        if (request.getStatus() == User.UserStatus.LOCKED) {
            freezeUserAccounts(userId);
        }

        adminAuditHelper.log("用户管理", "修改用户状态",
                "USER", String.valueOf(userId),
                oldStatus + " -> " + request.getStatus());
        log.info("管理员修改用户状态: userId={}, {} -> {}", userId, oldStatus, request.getStatus());
    }

    private void freezeUserAccounts(Long userId) {
        for (Account account : accountRepository.findByUserId(userId)) {
            if (account.getStatus() == AccountStatus.ACTIVE) {
                account.setStatus(AccountStatus.FROZEN);
                accountRepository.save(account);
                log.info("用户冻结联动：账户 {} 已冻结", account.getAccountNumber());
            }
        }
    }

    @Override
    @Transactional
    public String resetUserPassword(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(400, "用户不存在，ID: " + userId));

        String newPassword = generateRandomPassword(12);
        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserRepository.save(user);

        adminAuditHelper.log("用户管理", "重置用户密码",
                "USER", String.valueOf(userId), "已重置为随机密码");
        log.info("管理员重置用户密码: userId={}", userId);
        return newPassword;
    }

    @Override
    @Transactional
    public void setUserPassword(Long userId, String newPassword) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(400, "用户不存在，ID: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserRepository.save(user);

        adminAuditHelper.log("用户管理", "设置用户密码",
                "USER", String.valueOf(userId), "管理员手动设置新密码");
        log.info("管理员设置用户密码: userId={}", userId);
    }

    @Override
    @Transactional
    public UserVO createAdmin(CreateAdminRequest request) {
        if (adminUserRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (adminUserRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(400, "邮箱已被注册");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && adminUserRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(400, "手机号已被注册");
        }

        User admin = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .role(User.Role.ROLE_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();
        User saved = adminUserRepository.save(admin);

        adminAuditHelper.log("管理员管理", "新增管理员",
                "USER", String.valueOf(saved.getId()), "username=" + saved.getUsername());
        log.info("新增管理员: userId={}, username={}", saved.getId(), saved.getUsername());
        return UserVO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void disableUser(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(400, "用户不存在，ID: " + userId));

        if (user.getRole() == User.Role.ROLE_ADMIN) {
            throw new BusinessException(400, "不能禁用管理员账号");
        }

        user.setStatus(User.UserStatus.DISABLED);
        adminUserRepository.save(user);

        adminAuditHelper.log("用户管理", "禁用用户",
                "USER", String.valueOf(userId), null);
        log.info("管理员禁用用户: userId={}", userId);
    }

    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }
}
