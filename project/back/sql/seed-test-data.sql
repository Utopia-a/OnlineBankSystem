-- ============================================================
-- 网上银行系统 - 测试用户与账户种子数据
-- 数据库：banking_db
--
-- 用法：
--   mysql -uroot -p banking_db < sql/seed-test-data.sql
--
-- 说明：
--   - 不会删除 admin 管理员
--   - 可重复执行（先清理同名测试用户再插入）
--   - 测试用户密码依次为 Password1 ~ Password6（BCrypt cost=12）
-- ============================================================

USE banking_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 各测试用户密码 BCrypt 哈希
SET @pwd1 := '$2a$12$6LimCHgcozWbbWFa/ROwE.jqhPUcdwLwtgMNB7RFCqgPAC1is.c/G';  -- Password1 zhangsan
SET @pwd2 := '$2a$12$Uv2PYpOnH2DO.XXcVVfLo.p5Azk8vFFnF4CjbXX1IOGvtqHjdoQTS';  -- Password2 lisi
SET @pwd3 := '$2a$12$saQeMOAmx5okK3MD4E2AouwLZlZiTDFHe3YomoAcaZKvEpadoUHKy';  -- Password3 wangwu
SET @pwd4 := '$2a$12$5aZdeSoMET2DuujfLLAwqechCCNXrkEMtU3w/Lg8ATe4YihlfhXHK';  -- Password4 zhaoliu
SET @pwd5 := '$2a$12$6cu0.km/bQ0kyNY2Yh8yk.EW45eQtDvme0f5JxKKnrhaaqeOGgS3i';  -- Password5 sunqi
SET @pwd6 := '$2a$12$qvmWLfpy5pcomVW1jOdzM.wj.36dcHFoDMl01iQf7EYde6tqgn9Lq';  -- Password6 disabled_user

-- ------------------------------------------------------------
-- 1. 清理旧测试数据（按用户名，保留 admin）
-- ------------------------------------------------------------
DELETE rt FROM refresh_tokens rt
INNER JOIN users u ON rt.user_id = u.id
WHERE u.username IN ('zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'sunqi', 'disabled_user');

DELETE t FROM transactions t
INNER JOIN accounts a ON t.from_account_id = a.id OR t.to_account_id = a.id
INNER JOIN users u ON a.user_id = u.id
WHERE u.username IN ('zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'sunqi', 'disabled_user');

DELETE a FROM accounts a
INNER JOIN users u ON a.user_id = u.id
WHERE u.username IN ('zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'sunqi', 'disabled_user');

DELETE FROM users
WHERE username IN ('zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'sunqi', 'disabled_user');

-- ------------------------------------------------------------
-- 2. 插入测试用户
-- ------------------------------------------------------------
INSERT INTO users (username, email, phone, password, full_name, role, status, email_verified, failed_login_attempts, locked_until)
VALUES
-- 主测试账号：双账户，用于转账/查询（Password1）
('zhangsan', 'zhangsan@test.com', '13800000001', @pwd1, '张三', 'ROLE_USER', 'ACTIVE', 1, 0, NULL),
-- 收款方测试账号（Password2）
('lisi',     'lisi@test.com',     '13800000002', @pwd2, '李四', 'ROLE_USER', 'ACTIVE', 1, 0, NULL),
-- 低余额账号：测试余额不足（Password3）
('wangwu',   'wangwu@test.com',   '13800000003', @pwd3, '王五', 'ROLE_USER', 'ACTIVE', 1, 0, NULL),
-- 未验证邮箱：登录应提示先验证（Password4）
('zhaoliu',  'zhaoliu@test.com',  '13800000004', @pwd4, '赵六', 'ROLE_USER', 'PENDING_VERIFY', 0, 0, NULL),
-- 锁定账号：登录应提示账户锁定（Password5）
('sunqi',    'sunqi@test.com',    '13800000005', @pwd5, '孙七', 'ROLE_USER', 'LOCKED', 1, 5, DATE_ADD(NOW(6), INTERVAL 30 MINUTE)),
-- 禁用账号：登录应提示已禁用（Password6）
('disabled_user', 'disabled@test.com', '13800000006', @pwd6, '周八', 'ROLE_USER', 'DISABLED', 1, 0, NULL);

-- ------------------------------------------------------------
-- 3. 插入测试账户
-- ------------------------------------------------------------
INSERT INTO accounts (account_number, user_id, account_type, status, balance, currency, alias, daily_transfer_limit, version)
SELECT 'ACC202606120000001', id, 'CHECKING', 'ACTIVE', 10100.00, 'CNY', '张三活期', 50000.00, 0
FROM users WHERE username = 'zhangsan';

INSERT INTO accounts (account_number, user_id, account_type, status, balance, currency, alias, daily_transfer_limit, version)
SELECT 'ACC202606120000002', id, 'SAVINGS', 'ACTIVE', 5000.00, 'CNY', '张三储蓄', 50000.00, 0
FROM users WHERE username = 'zhangsan';

INSERT INTO accounts (account_number, user_id, account_type, status, balance, currency, alias, daily_transfer_limit, version)
SELECT 'ACC202606120000003', id, 'CHECKING', 'ACTIVE', 8000.00, 'CNY', '李四活期', 50000.00, 0
FROM users WHERE username = 'lisi';

INSERT INTO accounts (account_number, user_id, account_type, status, balance, currency, alias, daily_transfer_limit, version)
SELECT 'ACC202606120000004', id, 'CHECKING', 'ACTIVE', 100.00, 'CNY', '王五活期', 50000.00, 0
FROM users WHERE username = 'wangwu';

INSERT INTO accounts (account_number, user_id, account_type, status, balance, currency, alias, daily_transfer_limit, version)
SELECT 'ACC202606120000005', id, 'CHECKING', 'ACTIVE', 1000.00, 'CNY', '孙七活期', 50000.00, 0
FROM users WHERE username = 'sunqi';

-- ------------------------------------------------------------
-- 4. 插入示例交易记录（便于测试交易历史/导出）
-- ------------------------------------------------------------
INSERT INTO transactions (
    transaction_no, transaction_type, status,
    from_account_id, to_account_id, amount,
    from_balance_before, from_balance_after,
    to_balance_before, to_balance_after,
    remark, operator_id, operator_ip, created_at
)
SELECT
    'TXN202606120000000001', 'TRANSFER_OUT', 'SUCCESS',
    a_from.id, a_to.id, 500.00,
    10600.00, 10100.00,
    7500.00, 8000.00,
    '测试转账-张三转李四', u.id, '127.0.0.1', DATE_SUB(NOW(6), INTERVAL 2 DAY)
FROM users u
JOIN accounts a_from ON a_from.account_number = 'ACC202606120000001'
JOIN accounts a_to   ON a_to.account_number   = 'ACC202606120000003'
WHERE u.username = 'zhangsan';

INSERT INTO transactions (
    transaction_no, transaction_type, status,
    to_account_id, amount,
    to_balance_before, to_balance_after,
    remark, operator_id, operator_ip, created_at
)
SELECT
    'TXN202606120000000002', 'DEPOSIT', 'SUCCESS',
    a.id, 1000.00,
    9100.00, 10100.00,
    '测试存款', u.id, '127.0.0.1', DATE_SUB(NOW(6), INTERVAL 1 DAY)
FROM users u
JOIN accounts a ON a.account_number = 'ACC202606120000001'
WHERE u.username = 'zhangsan';

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 5. 验证查询（可选，执行后查看结果）
-- ------------------------------------------------------------
-- SELECT username, full_name, status, email_verified FROM users WHERE username != 'admin';
-- SELECT u.username, a.account_number, a.account_type, a.balance
-- FROM accounts a JOIN users u ON a.user_id = u.id
-- WHERE u.username IN ('zhangsan','lisi','wangwu')
-- ORDER BY u.username, a.account_number;
