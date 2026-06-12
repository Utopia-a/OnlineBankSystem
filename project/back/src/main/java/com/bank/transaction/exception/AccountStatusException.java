package com.bank.transaction.exception;

import com.bank.account.enums.AccountStatus;

/**
 * 账户状态异常（冻结、注销等）
 */
public class AccountStatusException extends BusinessException {

    public AccountStatusException(AccountStatus status) {
        super(4004, messageFor(status));
    }

    private static String messageFor(AccountStatus status) {
        if (status == AccountStatus.FROZEN) {
            return "账户已冻结，无法进行转账、存款或取款操作";
        }
        if (status == AccountStatus.CLOSED) {
            return "账户已注销，无法进行操作";
        }
        return "账户当前状态不可操作：" + status;
    }
}
