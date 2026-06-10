package com.bank.transaction.exception;

/**
 * 超出交易限额异常
 */
public class TransactionLimitException extends BusinessException {
    public TransactionLimitException(String message) {
        super(4003, message);
    }
}
