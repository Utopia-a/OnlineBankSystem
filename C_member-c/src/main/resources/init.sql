-- 网上银行系统 - 成员 C 账户管理模块
-- 数据库初始化脚本
-- MySQL 8.0 / UTF-8mb4 / LF

CREATE DATABASE IF NOT EXISTS bank_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bank_db;

-- ─────────────────────────────────────────────────────────────────────────────
-- 账户表
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS accounts
(
    id                   BIGINT          NOT NULL AUTO_INCREMENT COMMENT '账户主键',
    account_number       VARCHAR(20)     NOT NULL COMMENT '账户号码（系统生成，唯一）',
    user_id              BIGINT          NOT NULL COMMENT '关联用户ID（来自认证服务）',
    account_type         VARCHAR(20)     NOT NULL COMMENT '账户类型：SAVINGS/CHECKING/FIXED_DEPOSIT',
    status               VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT '账户状态：ACTIVE/FROZEN/CLOSED',
    balance              DECIMAL(18, 2)  NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    currency             VARCHAR(10)     NOT NULL DEFAULT 'CNY' COMMENT '货币类型',
    alias                VARCHAR(50)     NULL COMMENT '账户别名（用户自定义）',
    daily_transfer_limit DECIMAL(18, 2)  NOT NULL DEFAULT 50000.00 COMMENT '日转账限额',
    remark               VARCHAR(255)    NULL COMMENT '备注',
    created_at           DATETIME(6)     NOT NULL COMMENT '创建时间',
    updated_at           DATETIME(6)     NULL COMMENT '最后更新时间',
    closed_at            DATETIME(6)     NULL COMMENT '注销时间',
    version              BIGINT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (id),
    UNIQUE KEY uk_account_number (account_number),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_user_status (user_id, status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '银行账户表';

-- ─────────────────────────────────────────────────────────────────────────────
-- 测试数据（开发环境使用）
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO accounts (account_number, user_id, account_type, status, balance, currency,
                      alias, daily_transfer_limit, remark, created_at, updated_at, version)
VALUES ('ACC202401010000001', 1, 'SAVINGS', 'ACTIVE', 50000.00, 'CNY',
        '主储蓄账户', 50000.00, '测试账户', NOW(), NOW(), 0),
       ('ACC202401010000002', 1, 'CHECKING', 'ACTIVE', 10000.00, 'CNY',
        '活期账户', 20000.00, '测试账户', NOW(), NOW(), 0),
       ('ACC202401010000003', 2, 'SAVINGS', 'FROZEN', 8000.00, 'CNY',
        NULL, 50000.00, '冻结测试', NOW(), NOW(), 0);
