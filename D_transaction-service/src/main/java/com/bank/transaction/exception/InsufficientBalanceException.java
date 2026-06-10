package com.bank.transaction.exception;

/**
 * 余额不足异常
 */
public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException() {
        super(4002, "账户余额不足，无法完成本次交易");
    }
    public InsufficientBalanceException(String message) {
        super(4002, message);
    }
}
