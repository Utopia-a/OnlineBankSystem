package com.bank.account.exception;

/**
 * 账户状态异常（如账户已冻结/注销时尝试操作）
 */
public class AccountStatusException extends RuntimeException {

    public AccountStatusException(String message) {
        super(message);
    }
}
