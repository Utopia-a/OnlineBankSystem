package com.bank.admin.controller;

import com.bank.admin.dto.request.TransactionQueryRequest;
import com.bank.admin.dto.response.DashboardStatsVO;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.Result;
import com.bank.admin.dto.response.TransactionVO;
import com.bank.admin.service.TransactionMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-交易监控", description = "管理员查看和管理系统交易记录")
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TransactionMonitorController {

    private final TransactionMonitorService transactionMonitorService;

    @Operation(summary = "获取仪表盘统计数据", description = "包含用户统计、今日/本月交易统计")
    @GetMapping("/dashboard")
    public Result<DashboardStatsVO> getDashboardStats() {
        return Result.success(transactionMonitorService.getDashboardStats());
    }

    @Operation(summary = "分页查询交易记录", description = "支持按交易号、类型、状态、时间范围筛选")
    @GetMapping
    public Result<PageResult<TransactionVO>> listTransactions(TransactionQueryRequest request) {
        return Result.success(transactionMonitorService.listTransactions(request));
    }

    @Operation(summary = "查询交易详情")
    @GetMapping("/{transactionId}")
    public Result<TransactionVO> getTransactionById(
            @Parameter(description = "交易ID") @PathVariable Long transactionId) {
        return Result.success(transactionMonitorService.getTransactionById(transactionId));
    }

    @Operation(summary = "撤销交易", description = "仅可撤销PENDING状态的交易")
    @PostMapping("/{transactionId}/cancel")
    public Result<Void> cancelTransaction(
            @Parameter(description = "交易ID") @PathVariable Long transactionId) {
        transactionMonitorService.cancelTransaction(transactionId);
        return Result.success();
    }
}
