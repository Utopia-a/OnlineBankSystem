package com.banking.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/** 校验 seed-test-data.sql 中测试用户密码哈希 */
class SeedPasswordHashTest {

    private static final String PASSWORD1_HASH =
            "$2a$12$6LimCHgcozWbbWFa/ROwE.jqhPUcdwLwtgMNB7RFCqgPAC1is.c/G";
    private static final String PASSWORD2_HASH =
            "$2a$12$Uv2PYpOnH2DO.XXcVVfLo.p5Azk8vFFnF4CjbXX1IOGvtqHjdoQTS";
    private static final String PASSWORD3_HASH =
            "$2a$12$saQeMOAmx5okK3MD4E2AouwLZlZiTDFHe3YomoAcaZKvEpadoUHKy";
    private static final String PASSWORD4_HASH =
            "$2a$12$5aZdeSoMET2DuujfLLAwqechCCNXrkEMtU3w/Lg8ATe4YihlfhXHK";
    private static final String PASSWORD5_HASH =
            "$2a$12$6cu0.km/bQ0kyNY2Yh8yk.EW45eQtDvme0f5JxKKnrhaaqeOGgS3i";
    private static final String PASSWORD6_HASH =
            "$2a$12$qvmWLfpy5pcomVW1jOdzM.wj.36dcHFoDMl01iQf7EYde6tqgn9Lq";

    @Test
    void seedTestUserPasswordHashesShouldMatch() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        assertThat(encoder.matches("Password1", PASSWORD1_HASH)).isTrue();
        assertThat(encoder.matches("Password2", PASSWORD2_HASH)).isTrue();
        assertThat(encoder.matches("Password3", PASSWORD3_HASH)).isTrue();
        assertThat(encoder.matches("Password4", PASSWORD4_HASH)).isTrue();
        assertThat(encoder.matches("Password5", PASSWORD5_HASH)).isTrue();
        assertThat(encoder.matches("Password6", PASSWORD6_HASH)).isTrue();
    }
}
