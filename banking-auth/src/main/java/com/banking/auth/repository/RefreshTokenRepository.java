package com.banking.auth.repository;

import com.banking.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** 撤销某用户所有 token（登出所有设备） */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    /** 清理过期 token */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :before OR r.revoked = true")
    void deleteExpiredAndRevoked(@Param("before") LocalDateTime before);
}
