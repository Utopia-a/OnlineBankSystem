package com.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.banking", "com.bank", "com.banking.auth", "com.banking.report"
})
@EntityScan(basePackages = {
        "com.banking.auth.entity",
        "com.bank.account.entity",
        "com.bank.transaction.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.banking.auth.repository",
        "com.bank.account.repository",
        "com.bank.transaction.repository"
})
@EnableJpaAuditing
public class OnlineBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineBankApplication.class, args);
    }
}
