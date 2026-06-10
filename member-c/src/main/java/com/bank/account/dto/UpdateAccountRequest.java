package com.bank.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新账户信息请求 DTO
 */
@Data
@Schema(description = "更新账户信息请求")
public class UpdateAccountRequest {

    @Size(max = 50, message = "别名最多50个字符")
    @Schema(description = "账户别名", example = "我的主账户")
    private String alias;

    @DecimalMin(value = "0.01", message = "日转账限额必须大于0")
    @DecimalMax(value = "1000000.00", message = "日转账限额不能超过1000000")
    @Schema(description = "日转账限额", example = "50000.00")
    private BigDecimal dailyTransferLimit;

    @Size(max = 255, message = "备注最多255个字符")
    @Schema(description = "备注信息")
    private String remark;
}
