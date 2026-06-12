package com.bank.admin.service;

import com.bank.admin.dto.request.UpdateUserStatusRequest;
import com.bank.admin.dto.response.UserVO;
import com.bank.admin.repository.AdminUserRepository;
import com.bank.admin.service.impl.AdminUserServiceImpl;
import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.repository.AccountRepository;
import com.bank.admin.support.AdminAuditHelper;
import com.bank.transaction.exception.BusinessException;
import com.banking.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("管理员用户服务 - 单元测试")
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminAuditHelper adminAuditHelper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("testuser");
        normalUser.setEmail("test@test.com");
        normalUser.setStatus(User.UserStatus.ACTIVE);
        normalUser.setRole(User.Role.ROLE_USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@bank.com");
        adminUser.setStatus(User.UserStatus.ACTIVE);
        adminUser.setRole(User.Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("获取用户详情 - 用户存在时返回VO")
    void getUserById_whenUserExists_returnsVO() {
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        UserVO result = adminUserService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("获取用户详情 - 用户不存在时抛出BusinessException")
    void getUserById_whenUserNotFound_throwsException() {
        when(adminUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    @DisplayName("修改用户状态 - 正常用户可以锁定")
    void updateUserStatus_normalUser_canLock() {
        Account activeAccount = Account.builder()
                .id(10L).userId(1L).accountNumber("ACC001").status(AccountStatus.ACTIVE).build();
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(adminUserRepository.save(any(User.class))).thenReturn(normalUser);
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(activeAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(User.UserStatus.LOCKED);

        adminUserService.updateUserStatus(1L, request);

        verify(adminUserRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
        assertThat(normalUser.getStatus()).isEqualTo(User.UserStatus.LOCKED);
        assertThat(activeAccount.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }

    @Test
    @DisplayName("修改用户状态 - 不允许修改管理员状态")
    void updateUserStatus_adminUser_throwsException() {
        when(adminUserRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(User.UserStatus.LOCKED);

        assertThatThrownBy(() -> adminUserService.updateUserStatus(2L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能修改管理员账号状态");

        verify(adminUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("重置密码 - 成功生成新密码")
    void resetUserPassword_success() {
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(adminUserRepository.save(any(User.class))).thenReturn(normalUser);

        String newPassword = adminUserService.resetUserPassword(1L);

        assertThat(newPassword).isNotBlank();
        assertThat(newPassword).hasSizeGreaterThanOrEqualTo(12);
        verify(adminUserRepository).save(any(User.class));
    }

    @Test
    @DisplayName("禁用用户 - 不允许禁用管理员")
    void disableUser_adminUser_throwsException() {
        when(adminUserRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminUserService.disableUser(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能禁用管理员账号");

        verify(adminUserRepository, never()).save(any());
    }
}
