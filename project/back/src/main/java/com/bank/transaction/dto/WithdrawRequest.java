package com.bank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 取款请求 DTO
 */
@Data
@Schema(description = "取款请求参数")
public class WithdrawRequest {

    @NotNull(message = "账户ID不能为空")
    @Schema(description = "取款账户ID", example = "1001")
    private Long accountId;

    @NotNull(message = "取款金额不能为空")
    @DecimalMin(value = "0.01", message = "取款金额不能小于0.01")
    @Digits(integer = 15, fraction = 2, message = "金额格式不正确，最多2位小数")
    @Schema(description = "取款金额", example = "2000.00")
    private BigDecimal amount;

    @Size(max = 200, message = "备注最多200个字符")
    @Schema(description = "取款备注", example = "日常消费")
    private String remark;
}
