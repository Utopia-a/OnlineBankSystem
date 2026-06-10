package com.banking.auth;

import com.banking.auth.entity.User;
import com.banking.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
                "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 604800000L);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .email("test@example.com")
                .role(User.Role.ROLE_USER)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    @DisplayName("生成 Access Token 并解析用户名")
    void generateAndExtractUsername() {
        String token = jwtUtil.generateAccessToken(testUser);
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Token 有效性校验通过")
    void tokenIsValid() {
        String token = jwtUtil.generateAccessToken(testUser);
        assertThat(jwtUtil.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("伪造 Token 校验失败")
    void invalidToken_returnsFalse() {
        assertThat(jwtUtil.validateTokenSilently("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("Access Token 包含 role claim")
    void tokenContainsRole() {
        String token = jwtUtil.generateAccessToken(testUser);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Refresh Token 生成成功")
    void generateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(testUser);
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
    }
}
