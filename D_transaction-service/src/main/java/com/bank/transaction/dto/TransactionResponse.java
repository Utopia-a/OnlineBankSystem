package com.bank.transaction.dto;

import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易结果响应 DTO
 */
@Data
@Builder
@Schema(description = "交易操作响应")
public class TransactionResponse {

    @Schema(description = "交易记录ID")
    private Long id;

    @Schema(description = "业务流水号", example = "TXN20240610153012123456")
    private String transactionNo;

    @Schema(description = "交易类型")
    private TransactionType transactionType;

    @Schema(description = "交易状态")
    private TransactionStatus status;

    @Schema(description = "付款账户ID")
    private Long fromAccountId;

    @Schema(description = "收款账户ID")
    private Long toAccountId;

    @Schema(description = "交易金额")
    private BigDecimal amount;

    @Schema(description = "付款方交易后余额")
    private BigDecimal fromBalanceAfter;

    @Schema(description = "收款方交易后余额")
    private BigDecimal toBalanceAfter;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "交易时间")
    private LocalDateTime createdAt;

    @Schema(description = "失败原因（仅失败时返回）")
    private String failureReason;
}
