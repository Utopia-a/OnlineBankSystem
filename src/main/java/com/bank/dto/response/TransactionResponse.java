package com.bank.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录响应 DTO
 */
@Data
@Schema(description = "交易记录")
public class TransactionResponse {

    @Schema(description = "交易ID", example = "TXN20240101000001")
    private String transactionId;

    @Schema(description = "交易类型：TRANSFER-转账，DEPOSIT-存款，WITHDRAW-取款", example = "TRANSFER")
    private String transactionType;

    @Schema(description = "付款账户号", example = "6228480000000001")
    private String fromAccountNo;

    @Schema(description = "收款账户号", example = "6228480000000002")
    private String toAccountNo;

    @Schema(description = "交易金额（元）", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "交易前余额", example = "10000.00")
    private BigDecimal balanceBefore;

    @Schema(description = "交易后余额", example = "9500.00")
    private BigDecimal balanceAfter;

    @Schema(description = "交易状态：SUCCESS-成功，FAILED-失败，PENDING-处理中", example = "SUCCESS")
    private String status;

    @Schema(description = "备注", example = "房租")
    private String remark;

    @Schema(description = "交易时间")
    private LocalDateTime transactionTime;
}
