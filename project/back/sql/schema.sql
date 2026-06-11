-- ============================================================
-- 网上银行系统 - 统一数据库初始化脚本
-- 数据库：banking_db  |  MySQL 8.0  |  UTF-8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS banking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE banking_db;

-- ------------------------------------------------------------
-- 用户表（认证模块 B 维护）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    username              VARCHAR(50)  NOT NULL,
    password              VARCHAR(255) NOT NULL,
    email                 VARCHAR(100) NOT NULL,
    phone                 VARCHAR(20)  NULL,
    full_name             VARCHAR(100) NULL,
    role                  VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING_VERIFY',
    email_verified        TINYINT(1)   NOT NULL DEFAULT 0,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          DATETIME(6)  NULL,
    created_at            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)  NULL ON UPDATE CURRENT_TIMESTAMP(6),
    last_login_at         DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- Refresh Token 表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    token       VARCHAR(512) NOT NULL,
    user_id     BIGINT       NOT NULL,
    expires_at  DATETIME(6)  NOT NULL,
    revoked     TINYINT(1)   NOT NULL DEFAULT 0,
    client_info VARCHAR(255) NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_token (token),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Refresh Token';

-- ------------------------------------------------------------
-- OTP 验证码表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS otp_records (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    target     VARCHAR(100) NOT NULL,
    code       VARCHAR(10)  NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    used       TINYINT(1)   NOT NULL DEFAULT 0,
    expires_at DATETIME(6)  NOT NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_target_type (target, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OTP 记录';

-- ------------------------------------------------------------
-- 账户表（账户模块 C 维护，交易模块 D 读写）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS accounts (
    id                   BIGINT          NOT NULL AUTO_INCREMENT,
    account_number       VARCHAR(20)     NOT NULL,
    user_id              BIGINT          NOT NULL,
    account_type         VARCHAR(20)     NOT NULL,
    status               VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    balance              DECIMAL(18, 2)  NOT NULL DEFAULT 0.00,
    currency             VARCHAR(10)     NOT NULL DEFAULT 'CNY',
    alias                VARCHAR(50)     NULL,
    daily_transfer_limit DECIMAL(18, 2)  NOT NULL DEFAULT 50000.00,
    remark               VARCHAR(255)    NULL,
    created_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)     NULL ON UPDATE CURRENT_TIMESTAMP(6),
    closed_at            DATETIME(6)     NULL,
    version              BIGINT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_account_number (account_number),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='银行账户表';

-- ------------------------------------------------------------
-- 交易记录表（交易模块 D 维护，报表模块 E 只读）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id                   BIGINT         NOT NULL AUTO_INCREMENT,
    transaction_no       VARCHAR(32)    NOT NULL,
    transaction_type     VARCHAR(20)    NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    from_account_id      BIGINT         NULL,
    to_account_id        BIGINT         NULL,
    amount               DECIMAL(19, 2) NOT NULL,
    from_balance_before  DECIMAL(19, 2) NULL,
    from_balance_after   DECIMAL(19, 2) NULL,
    to_balance_before    DECIMAL(19, 2) NULL,
    to_balance_after     DECIMAL(19, 2) NULL,
    remark               VARCHAR(200)   NULL,
    failure_reason       VARCHAR(500)   NULL,
    operator_id          BIGINT         NULL,
    operator_ip          VARCHAR(50)    NULL,
    created_at           DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at           DATETIME(6)    NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_transaction_no (transaction_no),
    KEY idx_from_account (from_account_id),
    KEY idx_to_account (to_account_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- ------------------------------------------------------------
-- 系统配置表（管理员模块 F 维护）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    config_key   VARCHAR(100) NOT NULL,
    config_value TEXT         NOT NULL,
    description  VARCHAR(255) NULL,
    updated_by   BIGINT       NULL,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化系统配置
INSERT IGNORE INTO system_config (config_key, config_value, description) VALUES
('max_transfer_amount', '50000.00', '单笔最大转账金额（元）'),
('max_withdraw_amount', '20000.00', '单笔最大取款金额（元）'),
('daily_transfer_limit', '200000.00', '每日转账限额（元）'),
('login_max_retry',      '5',         '最大登录失败次数，超出则锁定账户'),
('account_lock_minutes', '30',        '账户锁定时长（分钟）');

-- 初始化管理员账号（密码：Admin@123）
INSERT IGNORE INTO users (username, email, password, full_name, status, role, email_verified) VALUES
('admin', 'admin@bank.com', '$2a$12$F12P8v5vI5133AEN7GlJRuRdF9fCIFVe2.Qk52QSk9hHtJ8u0NfL6', '系统管理员', 'ACTIVE', 'ROLE_ADMIN', 1);
