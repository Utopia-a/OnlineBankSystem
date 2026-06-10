package com.bank.account.exception;

import java.math.BigDecimal;

/**
 * 余额不足异常
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal available, BigDecimal required) {
        super(String.format("余额不足，当前余额: %.2f，需要: %.2f", available, required));
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
