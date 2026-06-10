package com.banking.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户信息实体（只读映射，对应成员C创建的 accounts 表）
 * 用于报表中关联账户号、账户名等信息
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 账户号（展示用） */
    @Column(name = "account_no", nullable = false, unique = true, length = 20)
    private String accountNo;

    /** 所属用户ID（关联 users.id） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 账户类型 */
    @Column(name = "account_type", length = 20)
    private String accountType;

    /** 当前余额 */
    @Column(precision = 18, scale = 2)
    private BigDecimal balance;

    /** 账户状态 */
    @Column(length = 20)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
