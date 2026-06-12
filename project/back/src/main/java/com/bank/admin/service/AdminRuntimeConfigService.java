package com.bank.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 读取 system_config 中的运行时交易参数，不存在时回退 application.yml 默认值。
 */
@Service
@RequiredArgsConstructor
public class AdminRuntimeConfigService {

    private final SystemConfigService systemConfigService;

    public BigDecimal getMaxTransferAmount(BigDecimal fallback) {
        return parseDecimal("max_transfer_amount", fallback);
    }

    public BigDecimal getDailyTransferLimit(BigDecimal fallback) {
        return parseDecimal("daily_transfer_limit", fallback);
    }

    public BigDecimal getMaxWithdrawAmount(BigDecimal fallback) {
        return parseDecimal("max_withdraw_amount", fallback);
    }

    public BigDecimal getTransferFeeRate() {
        return parseDecimal("transfer_fee_rate", BigDecimal.ZERO);
    }

    private BigDecimal parseDecimal(String key, BigDecimal fallback) {
        String value = systemConfigService.getValueByKey(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
