-- 修复 otp_records.type 列：支持 TRANSFER_VERIFY 等新 OTP 类型
-- 错误：Data truncated for column 'type' at row 1
USE banking_db;

ALTER TABLE otp_records
    MODIFY COLUMN type VARCHAR(30) NOT NULL COMMENT 'OTP类型: EMAIL_VERIFY, LOGIN_MFA, PASSWORD_RESET, TRANSFER_VERIFY';
