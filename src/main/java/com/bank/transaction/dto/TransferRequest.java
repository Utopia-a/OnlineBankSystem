package com.bank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 转账请求 DTO
 */
@Data
@Schema(description = "转账请求参数")
public class TransferRequest {

    @NotNull(message = "付款账户ID不能为空")
    @Schema(description = "付款方账户ID", example = "1001")
    private Long fromAccountId;

    @NotNull(message = "收款账户ID不能为空")
    @Schema(description = "收款方账户ID", example = "1002")
    private Long toAccountId;

    @NotNull(message = "转账金额不能为空")
    @DecimalMin(value = "0.01", message = "转账金额不能小于0.01")
    @Digits(integer = 15, fraction = 2, message = "金额格式不正确，最多2位小数")
    @Schema(description = "转账金额", example = "1000.00")
    private BigDecimal amount;

    @Size(max = 200, message = "备注最多200个字符")
    @Schema(description = "转账备注", example = "房租转账")
    private String remark;
}
