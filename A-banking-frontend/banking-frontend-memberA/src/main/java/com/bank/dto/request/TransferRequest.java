package com.bank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 转账请求 DTO
 */
@Data
@Schema(description = "转账请求")
public class TransferRequest {

    @NotBlank(message = "付款账户不能为空")
    @Schema(description = "付款账户号", example = "6228480000000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fromAccountNo;

    @NotBlank(message = "收款账户不能为空")
    @Schema(description = "收款账户号", example = "6228480000000002", requiredMode = Schema.RequiredMode.REQUIRED)
    private String toAccountNo;

    @NotNull(message = "转账金额不能为空")
    @DecimalMin(value = "0.01", message = "转账金额最小为0.01元")
    @DecimalMax(value = "1000000.00", message = "单笔转账最大为100万元")
    @Schema(description = "转账金额（元）", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Size(max = 100, message = "备注最多100个字符")
    @Schema(description = "转账备注", example = "房租")
    private String remark;

    @Schema(description = "OTP 验证码（大额转账时必填）", example = "654321")
    private String otpCode;
}
