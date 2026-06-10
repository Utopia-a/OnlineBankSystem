package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网上银行系统 - 成员A：前端 & 集成模块启动类
 * 职责：前端页面渲染、后端 API 代理转发、Swagger 接口文档
 */
@SpringBootApplication
public class BankingFrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingFrontendApplication.class, args);
    }
}
