package com.bank.account.exception;

/**
 * 账户未找到异常
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(Long id) {
        super("账户不存在，ID: " + id);
    }

    public AccountNotFoundException(String field, String value) {
        super("账户不存在，" + field + ": " + value);
    }
}
