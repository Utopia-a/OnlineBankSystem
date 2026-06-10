package com.bank.admin.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardStatsVO {

    private long totalUsers;
    private long activeUsers;
    private long frozenUsers;
    private long newUsersToday;

    private long todayTransactionCount;
    private BigDecimal todayTransactionAmount;
    private long todaySuccessCount;
    private long todayFailedCount;

    private long monthTransactionCount;
    private BigDecimal monthTransactionAmount;

    private long depositCount;
    private BigDecimal depositAmount;
    private long withdrawalCount;
    private BigDecimal withdrawalAmount;
    private long transferCount;
    private BigDecimal transferAmount;
}
