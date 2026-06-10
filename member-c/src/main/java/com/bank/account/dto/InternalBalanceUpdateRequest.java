package com.bank.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 内部余额更新请求 DTO（供成员 D 交易服务调用）
 */
@Data
@Schema(description = "内部余额更新请求（仅限内部服务调用）")
public class InternalBalanceUpdateRequest {

    @NotBlank(message = "账户号码不能为空")
    @Schema(description = "账户号码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountNumber;

    @NotNull(message = "金额不能为空")
    @Digits(integer = 16, fraction = 2, message = "金额格式不正确")
    @Schema(description = "变动金额（正为存入，负为扣除）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作类型：DEPOSIT/WITHDRAW/TRANSFER_IN/TRANSFER_OUT",
            example = "DEPOSIT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String operationType;

    @Schema(description = "关联交易号")
    private String transactionRef;
}
