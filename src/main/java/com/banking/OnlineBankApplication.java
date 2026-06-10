package com.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.banking", "com.bank"})
@EntityScan(basePackages = {"com.banking", "com.bank"})
@EnableJpaRepositories(basePackages = {"com.banking", "com.bank"})
@EnableJpaAuditing
public class OnlineBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineBankApplication.class, args);
    }
}
