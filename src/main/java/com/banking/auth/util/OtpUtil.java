package com.banking.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * OTP 工具类 - 生成随机数字验证码
 */
@Component
public class OtpUtil {

    @Value("${otp.length:6}")
    private int otpLength;

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成指定长度的数字 OTP
     */
    public String generateOtp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 格式化 OTP 用于展示（如 123-456）
     */
    public String formatOtp(String otp) {
        if (otp.length() == 6) {
            return otp.substring(0, 3) + "-" + otp.substring(3);
        }
        return otp;
    }
}
