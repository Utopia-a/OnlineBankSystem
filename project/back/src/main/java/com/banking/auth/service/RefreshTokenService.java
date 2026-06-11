package com.banking.auth.service;

import com.banking.auth.entity.RefreshToken;
import com.banking.auth.entity.User;
import com.banking.auth.exception.AuthException;
import com.banking.auth.repository.RefreshTokenRepository;
import com.banking.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token 管理服务（数据库持久化，不依赖 Redis）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    /**
     * 创建并持久化 Refresh Token
     */
    @Transactional
    public RefreshToken create(User user, String clientInfo) {
        // 每个用户最多保留 5 个 Token（可调整），先撤销旧的
        // 简化版：直接保存新 token
        String tokenValue = UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000);

        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(expiresAt)
                .clientInfo(clientInfo != null && clientInfo.length() > 200
                        ? clientInfo.substring(0, 200) : clientInfo)
                .build();

        return refreshTokenRepository.save(token);
    }

    /**
     * 验证 Refresh Token 并返回实体
     */
    @Transactional(readOnly = true)
    public RefreshToken verify(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new AuthException.InvalidTokenException("Refresh Token 不存在"));

        if (!token.isValid()) {
            throw new AuthException.InvalidTokenException(
                    token.getRevoked() ? "Refresh Token 已撤销" : "Refresh Token 已过期");
        }
        return token;
    }

    /**
     * 撤销单个 Token
     */
    @Transactional
    public void revoke(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    /**
     * 撤销用户所有 Token（登出所有设备）
     */
    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /**
     * 定时清理（每天凌晨 2 点）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
        log.info("过期 Refresh Token 清理完成");
    }
}
