package com.bank.transaction.entity;

import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录实体
 * 对应数据库表：transactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_from_account", columnList = "from_account_id"),
        @Index(name = "idx_to_account", columnList = "to_account_id"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_transaction_no", columnList = "transaction_no", unique = true)
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务流水号，全局唯一，格式：TXN + yyyyMMddHHmmss + 6位随机数
     */
    @Column(name = "transaction_no", nullable = false, length = 32, unique = true)
    private String transactionNo;

    /**
     * 交易类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    /**
     * 交易状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    /**
     * 付款方账户ID（取款/转账时有值，存款时为null）
     */
    @Column(name = "from_account_id")
    private Long fromAccountId;

    /**
     * 收款方账户ID（存款/转账时有值，取款时为null）
     */
    @Column(name = "to_account_id")
    private Long toAccountId;

    /**
     * 交易金额（正数，精确到分）
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * 交易前余额快照（付款方）
     */
    @Column(name = "from_balance_before", precision = 19, scale = 2)
    private BigDecimal fromBalanceBefore;

    /**
     * 交易后余额快照（付款方）
     */
    @Column(name = "from_balance_after", precision = 19, scale = 2)
    private BigDecimal fromBalanceAfter;

    /**
     * 交易前余额快照（收款方）
     */
    @Column(name = "to_balance_before", precision = 19, scale = 2)
    private BigDecimal toBalanceBefore;

    /**
     * 交易后余额快照（收款方）
     */
    @Column(name = "to_balance_after", precision = 19, scale = 2)
    private BigDecimal toBalanceAfter;

    /**
     * 交易备注
     */
    @Column(name = "remark", length = 200)
    private String remark;

    /**
     * 失败原因（失败时记录）
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * 操作人（用户ID，来自JWT）
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 操作人IP地址
     */
    @Column(name = "operator_ip", length = 50)
    private String operatorIp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
