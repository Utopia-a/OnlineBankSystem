package com.banking.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 成员E - 账单与报表模块启动类
 * 职责：交易历史查询、分页、导出（Excel / CSV）
 * 端口：8085，Context Path：/api
 */
@SpringBootApplication
public class BankingReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingReportApplication.class, args);
    }
}
