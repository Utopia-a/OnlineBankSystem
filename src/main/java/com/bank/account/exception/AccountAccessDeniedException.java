package com.bank.account.exception;

/**
 * 无权限访问他人账户异常
 */
public class AccountAccessDeniedException extends RuntimeException {

    public AccountAccessDeniedException() {
        super("无权限访问该账户");
    }

    public AccountAccessDeniedException(String message) {
        super(message);
    }
}
