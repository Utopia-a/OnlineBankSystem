package com.bank.admin.service.impl;

import com.bank.admin.dto.request.UpdateUserStatusRequest;
import com.bank.admin.dto.request.UserQueryRequest;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.UserVO;
import com.bank.admin.repository.AdminUserRepository;
import com.bank.admin.service.AdminUserService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

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
        adminUserRepository.save(user);

        log.info("管理员修改用户状态: userId={}, {} -> {}", userId, oldStatus, request.getStatus());
    }

    @Override
    @Transactional
    public String resetUserPassword(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(400, "用户不存在，ID: " + userId));

        String newPassword = generateRandomPassword(12);
        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserRepository.save(user);

        log.info("管理员重置用户密码: userId={}", userId);
        return newPassword;
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
