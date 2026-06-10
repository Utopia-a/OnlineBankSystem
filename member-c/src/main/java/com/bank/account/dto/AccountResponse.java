package com.bank.account.dto;

import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户信息响应 DTO
 */
@Data
@Schema(description = "账户信息响应")
public class AccountResponse {

    @Schema(description = "账户ID")
    private Long id;

    @Schema(description = "账户号码", example = "ACC20240001000001")
    private String accountNumber;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "账户类型")
    private AccountType accountType;

    @Schema(description = "账户状态")
    private AccountStatus status;

    @Schema(description = "账户余额", example = "10000.00")
    private BigDecimal balance;

    @Schema(description = "货币类型", example = "CNY")
    private String currency;

    @Schema(description = "账户别名", example = "我的储蓄账户")
    private String alias;

    @Schema(description = "日转账限额", example = "50000.00")
    private BigDecimal dailyTransferLimit;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "关闭时间")
    private LocalDateTime closedAt;

    /**
     * 从实体转换
     */
    public static AccountResponse fromEntity(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setUserId(account.getUserId());
        response.setAccountType(account.getAccountType());
        response.setStatus(account.getStatus());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setAlias(account.getAlias());
        response.setDailyTransferLimit(account.getDailyTransferLimit());
        response.setRemark(account.getRemark());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        response.setClosedAt(account.getClosedAt());
        return response;
    }
}
