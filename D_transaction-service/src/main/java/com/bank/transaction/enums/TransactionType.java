package com.bank.transaction.enums;

/**
 * 交易类型枚举
 */
public enum TransactionType {
    /** 转账（汇出）*/
    TRANSFER_OUT,
    /** 转账（汇入）*/
    TRANSFER_IN,
    /** 存款 */
    DEPOSIT,
    /** 取款 */
    WITHDRAWAL
}
