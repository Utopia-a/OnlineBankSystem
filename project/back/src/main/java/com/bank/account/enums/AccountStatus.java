package com.bank.account.enums;

/**
 * 账户状态枚举
 */
public enum AccountStatus {

    /**
     * 活跃 - 正常使用
     */
    ACTIVE,

    /**
     * 冻结 - 临时冻结，不可存取款
     */
    FROZEN,

    /**
     * 已注销 - 永久关闭
     */
    CLOSED
}
