package com.bank.transaction.enums;

/**
 * 交易状态枚举
 */
public enum TransactionStatus {
    /** 处理中（事务未提交）*/
    PENDING,
    /** 成功 */
    SUCCESS,
    /** 失败 */
    FAILED,
    /** 已回滚 */
    ROLLED_BACK
}
