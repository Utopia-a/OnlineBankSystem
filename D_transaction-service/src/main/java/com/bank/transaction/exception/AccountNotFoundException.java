package com.bank.transaction.exception;

/**
 * 账户不存在异常
 */
public class AccountNotFoundException extends BusinessException {
    public AccountNotFoundException(Long accountId) {
        super(4001, "账户不存在：" + accountId);
    }
    public AccountNotFoundException(String accountNo) {
        super(4001, "账户不存在：" + accountNo);
    }
}
