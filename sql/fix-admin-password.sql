-- 修复已有库中 admin 账号密码（明文密码：Admin@123）
-- 用法：mysql -uroot -p banking_db < sql/fix-admin-password.sql

USE banking_db;

UPDATE users
SET password = '$2a$12$F12P8v5vI5133AEN7GlJRuRdF9fCIFVe2.Qk52QSk9hHtJ8u0NfL6',
    status = 'ACTIVE',
    role = 'ROLE_ADMIN',
    email_verified = 1
WHERE username = 'admin';

INSERT INTO users (username, email, password, full_name, status, role, email_verified)
SELECT 'admin', 'admin@bank.com',
       '$2a$12$F12P8v5vI5133AEN7GlJRuRdF9fCIFVe2.Qk52QSk9hHtJ8u0NfL6',
       '系统管理员', 'ACTIVE', 'ROLE_ADMIN', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
