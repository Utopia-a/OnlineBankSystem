package com.bank.account.dto;

import com.bank.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建账户请求 DTO
 */
@Data
@Schema(description = "创建账户请求")
public class CreateAccountRequest {

    @NotNull(message = "账户类型不能为空")
    @Schema(description = "账户类型", example = "SAVINGS", requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountType accountType;

    @DecimalMin(value = "0.00", message = "初始存款不能为负数")
    @Digits(integer = 16, fraction = 2, message = "金额格式不正确")
    @Schema(description = "初始存款金额", example = "1000.00")
    private BigDecimal initialDeposit = BigDecimal.ZERO;

    @Size(max = 50, message = "别名最多50个字符")
    @Schema(description = "账户别名", example = "我的储蓄账户")
    private String alias;

    @Size(max = 3, min = 3, message = "货币代码必须为3位")
    @Schema(description = "货币类型", example = "CNY", defaultValue = "CNY")
    private String currency = "CNY";

    @DecimalMin(value = "0.01", message = "日转账限额必须大于0")
    @DecimalMax(value = "1000000.00", message = "日转账限额不能超过1000000")
    @Schema(description = "日转账限额", example = "50000.00")
    private BigDecimal dailyTransferLimit = new BigDecimal("50000.00");

    @Size(max = 255, message = "备注最多255个字符")
    @Schema(description = "备注信息")
    private String remark;
}
