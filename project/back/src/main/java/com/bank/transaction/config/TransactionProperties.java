package com.bank.transaction.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 交易业务规则配置（从 application.yml 注入）
 */
@Data
@Component
@ConfigurationProperties(prefix = "transaction")
public class TransactionProperties {

    private Transfer transfer = new Transfer();
    private Deposit deposit = new Deposit();
    private Withdraw withdraw = new Withdraw();

    @Data
    public static class Transfer {
        private BigDecimal dailyLimit = new BigDecimal("500000.00");
        private BigDecimal singleLimit = new BigDecimal("100000.00");
        private BigDecimal minAmount = new BigDecimal("0.01");
        /** 超过此金额需邮箱 OTP 验证 */
        private BigDecimal otpThreshold = new BigDecimal("5000.00");
    }

    @Data
    public static class Deposit {
        private BigDecimal maxAmount = new BigDecimal("500000.00");
        private BigDecimal minAmount = new BigDecimal("0.01");
    }

    @Data
    public static class Withdraw {
        private BigDecimal dailyLimit = new BigDecimal("50000.00");
        private BigDecimal singleLimit = new BigDecimal("20000.00");
        private BigDecimal minAmount = new BigDecimal("0.01");
    }
}
