package com.bank.transaction.config;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 交易流水号生成器
 * 格式：TXN + yyyyMMddHHmmssSSS + 6位随机数
 * 示例：TXN202406101530121234567890
 */
@Component
public class TransactionNoGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TXN" + timestamp + random;
    }
}
