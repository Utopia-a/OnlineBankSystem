package com.bank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 存款/取款请求 DTO
 */
@Data
@Schema(description = "存款/取款请求")
public class DepositWithdrawRequest {

    @NotBlank(message = "账户号不能为空")
    @Schema(description = "账户号", example = "6228480000000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountNo;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额最小为0.01元")
    @DecimalMax(value = "500000.00", message = "单次最大50万元")
    @Schema(description = "金额（元）", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Size(max = 100, message = "备注最多100个字符")
    @Schema(description = "备注")
    private String remark;
}
