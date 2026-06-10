package com.bank.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额查询响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额查询响应")
public class BalanceResponse {

    @Schema(description = "账户号码")
    private String accountNumber;

    @Schema(description = "当前余额", example = "10000.00")
    private BigDecimal balance;

    @Schema(description = "货币类型", example = "CNY")
    private String currency;

    @Schema(description = "查询时间")
    private LocalDateTime queryTime;

    public BalanceResponse(String accountNumber, BigDecimal balance, String currency) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.queryTime = LocalDateTime.now();
    }
}
