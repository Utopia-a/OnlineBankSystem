package com.bank.transaction.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Optional;

/**
 * JWT 工具类（仅做解析，签发由成员B负责）
 */
@Slf4j
@Component
public class JwtTokenUtil {

    private final Key signingKey;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从 Token 中解析用户ID
     */
    public Optional<Long> extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.ofNullable(claims.get("userId", Long.class));
        } catch (JwtException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 从 Token 中解析用户名
     */
    public Optional<String> extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 验证 Token 有效性
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
