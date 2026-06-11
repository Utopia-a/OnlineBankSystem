package com.bank.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户信息响应 DTO
 */
@Data
@Schema(description = "账户信息")
public class AccountResponse {

    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    @Schema(description = "账户号", example = "6228480000000001")
    private String accountNo;

    @Schema(description = "账户类型：SAVINGS-储蓄账户，CHECKING-活期账户", example = "SAVINGS")
    private String accountType;

    @Schema(description = "账户余额（元）", example = "10000.00")
    private BigDecimal balance;

    @Schema(description = "账户状态：ACTIVE-正常，FROZEN-冻结，CLOSED-注销", example = "ACTIVE")
    private String status;

    @Schema(description = "开户时间")
    private LocalDateTime createTime;

    @Schema(description = "货币类型", example = "CNY")
    private String currency;
}
