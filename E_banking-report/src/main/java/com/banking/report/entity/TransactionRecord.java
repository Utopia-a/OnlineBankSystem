package com.banking.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录实体（只读映射，对应成员D创建的 transactions 表）
 * 本模块不修改此表，仅做查询和导出
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 交易流水号（全局唯一） */
    @Column(name = "transaction_no", nullable = false, unique = true, length = 32)
    private String transactionNo;

    /** 发起方账户ID（转账时为付款账户，取款时为账户ID，存款时为 null） */
    @Column(name = "from_account_id")
    private Long fromAccountId;

    /** 接收方账户ID（转账时为收款账户，存款时为账户ID，取款时为 null） */
    @Column(name = "to_account_id")
    private Long toAccountId;

    /** 交易金额（正数） */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /** 交易前余额（发起方） */
    @Column(name = "balance_before", precision = 18, scale = 2)
    private BigDecimal balanceBefore;

    /** 交易后余额（发起方） */
    @Column(name = "balance_after", precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    /** 交易类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    /** 交易状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    /** 备注/摘要 */
    @Column(length = 200)
    private String remark;

    /** 操作人用户ID（发起交易的登录用户） */
    @Column(name = "operator_id")
    private Long operatorId;

    /** 交易时间 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== 枚举 =====

    public enum TransactionType {
        DEPOSIT,     // 存款
        WITHDRAWAL,  // 取款
        TRANSFER     // 转账
    }

    public enum TransactionStatus {
        SUCCESS,   // 成功
        FAILED,    // 失败
        PENDING    // 处理中
    }
}
