package com.banking.auth.repository;

import com.banking.auth.entity.OtpRecord;
import com.banking.auth.entity.OtpRecord.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRecordRepository extends JpaRepository<OtpRecord, Long> {

    /** 查找最新有效 OTP */
    @Query("SELECT o FROM OtpRecord o WHERE o.target = :target AND o.type = :type " +
           "AND o.used = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<OtpRecord> findLatestValid(
            @Param("target") String target,
            @Param("type") OtpType type,
            @Param("now") LocalDateTime now);

    /** 验证特定 code */
    @Query("SELECT o FROM OtpRecord o WHERE o.target = :target AND o.code = :code " +
           "AND o.type = :type AND o.used = false AND o.expiresAt > :now")
    Optional<OtpRecord> findValidByCode(
            @Param("target") String target,
            @Param("code") String code,
            @Param("type") OtpType type,
            @Param("now") LocalDateTime now);

    /** 使某 target+type 下所有旧 OTP 失效 */
    @Modifying
    @Query("UPDATE OtpRecord o SET o.used = true WHERE o.target = :target AND o.type = :type AND o.used = false")
    void invalidateAll(@Param("target") String target, @Param("type") OtpType type);

    /** 清理已过期记录（定时任务用） */
    @Modifying
    @Query("DELETE FROM OtpRecord o WHERE o.expiresAt < :before")
    void deleteExpiredBefore(@Param("before") LocalDateTime before);
}
