package com.bank.transaction.exception;

/**
 * 账户状态异常（冻结、注销等）
 */
public class AccountStatusException extends BusinessException {
    public AccountStatusException(String status) {
        super(4004, "账户当前状态不可操作：" + status);
    }
}
