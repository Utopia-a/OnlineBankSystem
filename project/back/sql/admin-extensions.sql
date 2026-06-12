-- 管理后台扩展：操作日志表 + 转账费率配置
USE banking_db;

CREATE TABLE IF NOT EXISTS operation_logs (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    operator_id   BIGINT       NOT NULL,
    operator_name VARCHAR(100) NULL,
    module        VARCHAR(50)  NOT NULL,
    action        VARCHAR(100) NOT NULL,
    target_type   VARCHAR(50)  NULL,
    target_id     VARCHAR(64)  NULL,
    detail        TEXT         NULL,
    ip_address    VARCHAR(45)  NULL,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_op_log_operator (operator_id),
    KEY idx_op_log_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志';

INSERT IGNORE INTO system_config (config_key, config_value, description) VALUES
('transfer_fee_rate', '0', '转账手续费率（小数，如 0.001 表示 0.1%）');
