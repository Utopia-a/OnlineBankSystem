package com.banking.auth;

import com.banking.auth.dto.AuthRequest;
import com.banking.auth.dto.AuthResponse.*;
import com.banking.auth.entity.OtpRecord.OtpType;
import com.banking.auth.entity.RefreshToken;
import com.banking.auth.entity.User;
import com.banking.auth.exception.AuthException;
import com.banking.auth.repository.UserRepository;
import com.banking.auth.service.AuthService;
import com.banking.auth.service.OtpService;
import com.banking.auth.service.RefreshTokenService;
import com.banking.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 单元测试")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock OtpService otpService;
    @Mock RefreshTokenService refreshTokenService;

    @InjectMocks
    AuthService authService;

    private AuthRequest.Register validRegisterRequest;
    private AuthRequest.Login validLoginRequest;
    private User activeUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new AuthRequest.Register();
        validRegisterRequest.setUsername("testuser");
        validRegisterRequest.setPassword("Password1");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setFullName("Test User");

        validLoginRequest = new AuthRequest.Login();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("Password1");

        activeUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    // ===== 注册测试 =====

    @Test
    @DisplayName("注册成功")
    void register_success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder().id(1L).username(u.getUsername()).email(u.getEmail())
                    .password(u.getPassword()).status(User.UserStatus.PENDING_VERIFY).build();
            return u;
        });
        when(otpService.generateAndSend(anyString(), eq(OtpType.EMAIL_VERIFY)))
                .thenReturn(new OtpResult(true, "te***@example.com", 300, "已发送"));

        RegisterResult result = authService.register(validRegisterRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.isEmailVerificationSent()).isTrue();
        verify(otpService).generateAndSend(eq("test@example.com"), eq(OtpType.EMAIL_VERIFY));
    }

    @Test
    @DisplayName("注册 - 用户名已存在")
    void register_usernameExists_throwsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(AuthException.UserAlreadyExistsException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    @DisplayName("注册 - 邮箱已存在")
    void register_emailExists_throwsException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(AuthException.UserAlreadyExistsException.class)
                .hasMessageContaining("邮箱已注册");
    }

    // ===== 登录测试 =====

    @Test
    @DisplayName("登录成功")
    void login_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password1", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(activeUser)).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L);
        RefreshToken rt = RefreshToken.builder().token("refresh-token")
                .user(activeUser).expiresAt(LocalDateTime.now().plusDays(7)).build();
        when(refreshTokenService.create(eq(activeUser), any())).thenReturn(rt);

        LoginResult result = authService.login(validLoginRequest, "Chrome");

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.isRequireMfa()).isFalse();
    }

    @Test
    @DisplayName("登录 - 密码错误")
    void login_wrongPassword_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(validLoginRequest, null))
                .isInstanceOf(AuthException.InvalidCredentialsException.class);
        verify(userRepository).incrementFailedAttempts(1L);
    }

    @Test
    @DisplayName("登录 - 账户未验证邮箱")
    void login_emailNotVerified_throwsException() {
        activeUser.setStatus(User.UserStatus.PENDING_VERIFY);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(validLoginRequest, null))
                .isInstanceOf(AuthException.AccountNotVerifiedException.class);
    }

    @Test
    @DisplayName("登录 - 账户锁定")
    void login_accountLocked_throwsException() {
        activeUser.setStatus(User.UserStatus.LOCKED);
        activeUser.setLockedUntil(LocalDateTime.now().plusMinutes(20));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(validLoginRequest, null))
                .isInstanceOf(AuthException.AccountLockedException.class);
    }

    // ===== 找回密码测试 =====

    @Test
    @DisplayName("找回密码 - 发送 OTP 成功")
    void forgotPassword_success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(otpService.hasValidOtp("test@example.com", OtpType.PASSWORD_RESET)).thenReturn(false);
        when(otpService.generateAndSend(anyString(), eq(OtpType.PASSWORD_RESET)))
                .thenReturn(new OtpResult(true, "te***@example.com", 300, "已发送"));

        OtpResult result = authService.forgotPassword("test@example.com");
        assertThat(result.isSent()).isTrue();
    }

    @Test
    @DisplayName("找回密码 - 频率限制")
    void forgotPassword_tooManyRequests_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(activeUser));
        when(otpService.hasValidOtp(anyString(), eq(OtpType.PASSWORD_RESET))).thenReturn(true);

        assertThatThrownBy(() -> authService.forgotPassword("test@example.com"))
                .isInstanceOf(AuthException.TooManyOtpRequestsException.class);
    }
}
