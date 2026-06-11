package com.banking.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class AdminPasswordHashTest {

    private static final String SCHEMA_HASH =
            "$2a$12$F12P8v5vI5133AEN7GlJRuRdF9fCIFVe2.Qk52QSk9hHtJ8u0NfL6";

    @Test
    void schemaAdminPasswordShouldMatchAdminAt123() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        assertThat(encoder.matches("Admin@123", SCHEMA_HASH))
                .as("schema.sql 中的 admin 密码哈希应与 Admin@123 匹配")
                .isTrue();
    }
}
