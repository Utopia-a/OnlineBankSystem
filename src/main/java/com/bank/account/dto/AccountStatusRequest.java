package com.bank.account.dto;

import com.bank.account.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 账户状态变更请求 DTO
 */
@Data
@Schema(description = "账户状态变更请求")
public class AccountStatusRequest {

    @NotNull(message = "账户状态不能为空")
    @Schema(description = "目标状态", example = "FROZEN", requiredMode = Schema.RequiredMode.REQUIRED)
    private AccountStatus status;

    @Size(max = 255, message = "原因说明最多255个字符")
    @Schema(description = "状态变更原因", example = "用户申请冻结")
    private String reason;
}
