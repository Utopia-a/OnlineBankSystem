package com.bank.account.entity;

import com.bank.account.enums.AccountStatus;
import com.bank.account.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行账户实体
 */
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "account_number", unique = true),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 账户号码（唯一，系统生成）
     */
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    /**
     * 关联用户ID（来自 Member B 认证服务）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 账户类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    /**
     * 账户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * 账户余额
     */
    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 货币类型（默认人民币）
     */
    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "CNY";

    /**
     * 账户别名（用户自定义）
     */
    @Column(name = "alias", length = 50)
    private String alias;

    /**
     * 日转账限额
     */
    @Column(name = "daily_transfer_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dailyTransferLimit = new BigDecimal("50000.00");

    /**
     * 备注
     */
    @Column(name = "remark", length = 255)
    private String remark;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 关闭时间
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @Column(name = "version")
    private Long version;
}
