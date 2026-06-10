package com.bank.account.config;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 账户号码生成器
 * 格式: ACC + yyyyMMdd + 6位序列号
 * 示例: ACC202401010000001
 */
@Component
public class AccountNumberGenerator {

    private static final String PREFIX = "ACC";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 简单序列，生产环境建议用数据库序列或分布式ID
    private final AtomicLong sequence = new AtomicLong(1L);

    public String generate() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        long seq = sequence.getAndIncrement();
        return String.format("%s%s%07d", PREFIX, date, seq);
    }
}
