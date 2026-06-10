-- ============================================================
-- 网上银行系统 - 成员D 交易服务 数据库脚本
-- 数据库：MySQL 8.0
-- 编码：UTF-8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS online_bank
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE online_bank;

-- ------------------------------------------------------------
-- 账户表（由成员C维护，此处提供参考结构）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '账户主键',
    account_no  VARCHAR(32)     NOT NULL                COMMENT '账户号（业务唯一）',
    user_id     BIGINT          NOT NULL                COMMENT '所属用户ID',
    balance     DECIMAL(19, 2)  NOT NULL DEFAULT 0.00   COMMENT '账户余额',
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/FROZEN/CLOSED',
    version     BIGINT          NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_no (account_no),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- ------------------------------------------------------------
-- 交易记录表（成员D维护）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id                   BIGINT         NOT NULL AUTO_INCREMENT COMMENT '交易主键',
    transaction_no       VARCHAR(32)    NOT NULL                COMMENT '业务流水号（全局唯一）',
    transaction_type     VARCHAR(20)    NOT NULL                COMMENT '交易类型：TRANSFER_OUT/TRANSFER_IN/DEPOSIT/WITHDRAWAL',
    status               VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/SUCCESS/FAILED/ROLLED_BACK',
    from_account_id      BIGINT                                 COMMENT '付款方账户ID',
    to_account_id        BIGINT                                 COMMENT '收款方账户ID',
    amount               DECIMAL(19, 2) NOT NULL                COMMENT '交易金额（正数）',
    from_balance_before  DECIMAL(19, 2)                         COMMENT '付款方交易前余额快照',
    from_balance_after   DECIMAL(19, 2)                         COMMENT '付款方交易后余额快照',
    to_balance_before    DECIMAL(19, 2)                         COMMENT '收款方交易前余额快照',
    to_balance_after     DECIMAL(19, 2)                         COMMENT '收款方交易后余额快照',
    remark               VARCHAR(200)                           COMMENT '交易备注',
    failure_reason       VARCHAR(500)                           COMMENT '失败原因',
    operator_id          BIGINT                                 COMMENT '操作人用户ID',
    operator_ip          VARCHAR(50)                            COMMENT '操作人IP',
    created_at           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transaction_no (transaction_no),
    KEY idx_from_account (from_account_id),
    KEY idx_to_account (to_account_id),
    KEY idx_created_at (created_at),
    KEY idx_from_account_created (from_account_id, created_at)  -- 用于单日限额查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- ------------------------------------------------------------
-- 初始化测试账户（开发/测试环境使用）
-- ------------------------------------------------------------
INSERT IGNORE INTO accounts (id, account_no, user_id, balance, status, version)
VALUES
    (1001, 'ACC10000001', 1, 10000.00, 'ACTIVE',  0),
    (1002, 'ACC10000002', 2, 5000.00,  'ACTIVE',  0),
    (1003, 'ACC10000003', 3, 0.00,     'FROZEN',  0);
