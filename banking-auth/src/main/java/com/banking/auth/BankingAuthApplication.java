package com.banking.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 成员B - 认证与安全模块启动类
 * 负责：注册/登录、JWT、Spring Security 配置、OTP
 */
@SpringBootApplication
@EnableScheduling
public class BankingAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingAuthApplication.class, args);
    }
}
