package com.banking.auth.service;

import com.banking.auth.dto.AuthResponse.OtpResult;
import com.banking.auth.entity.OtpRecord;
import com.banking.auth.entity.OtpRecord.OtpType;
import com.banking.auth.exception.AuthException;
import com.banking.auth.repository.OtpRecordRepository;
import com.banking.auth.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * OTP 服务
 * 生成、验证、清理一次性密码
 * 注：真实项目应集成邮件/短信服务，此处以日志模拟发送
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRecordRepository otpRecordRepository;
    private final OtpUtil otpUtil;

    @Value("${otp.expiration:300000}")
    private long otpExpiration; // ms

    /**
     * 生成并发送 OTP
     *
     * @param target 邮箱或手机号
     * @param type   OTP 类型
     * @return OtpResult
     */
    @Transactional
    public OtpResult generateAndSend(String target, OtpType type) {
        // 使旧 OTP 失效
        otpRecordRepository.invalidateAll(target, type);

        String code = otpUtil.generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(otpExpiration / 1000);

        OtpRecord record = OtpRecord.builder()
                .target(target)
                .code(code)
                .type(type)
                .expiresAt(expiresAt)
                .build();
        otpRecordRepository.save(record);

        // TODO: 集成邮件/短信服务，此处模拟输出
        log.info("[OTP 模拟发送] target={} type={} code={} expires={}",
                maskTarget(target), type, code, expiresAt);

        return OtpResult.builder()
                .sent(true)
                .maskedTarget(maskTarget(target))
                .expiresInSeconds(otpExpiration / 1000)
                .message("验证码已发送至 " + maskTarget(target))
                .build();
    }

    /**
     * 验证 OTP
     *
     * @param target 目标
     * @param code   用户输入的 code
     * @param type   类型
     * @throws AuthException.InvalidOtpException 验证失败
     */
    @Transactional
    public void verify(String target, String code, OtpType type) {
        OtpRecord record = otpRecordRepository
                .findValidByCode(target, code, type, LocalDateTime.now())
                .orElseThrow(() -> new AuthException.InvalidOtpException("验证码无效或已过期"));

        record.setUsed(true);
        otpRecordRepository.save(record);
    }

    /**
     * 检查是否存在尚未过期的有效 OTP（防止频繁发送）
     */
    @Transactional(readOnly = true)
    public boolean hasValidOtp(String target, OtpType type) {
        return otpRecordRepository
                .findLatestValid(target, type, LocalDateTime.now())
                .isPresent();
    }

    /**
     * 定时清理过期 OTP（每小时一次）
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanExpiredOtp() {
        otpRecordRepository.deleteExpiredBefore(LocalDateTime.now().minusMinutes(10));
        log.debug("过期 OTP 清理完成");
    }

    /**
     * 脱敏目标（邮箱 / 手机号）
     */
    public String maskTarget(String target) {
        if (target == null) return "";
        if (target.contains("@")) {
            // 邮箱脱敏：us***@domain.com
            int atIndex = target.indexOf('@');
            int visibleLen = Math.min(2, atIndex);
            return target.substring(0, visibleLen) + "***" + target.substring(atIndex);
        }
        // 手机号脱敏：138****1234
        if (target.length() >= 7) {
            return target.substring(0, 3) + "****" + target.substring(target.length() - 4);
        }
        return "****";
    }
}
