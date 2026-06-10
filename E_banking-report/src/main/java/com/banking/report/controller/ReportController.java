package com.banking.report.controller;

import com.banking.report.dto.ReportResponse.*;
import com.banking.report.dto.TransactionQueryRequest;
import com.banking.report.service.ReportService;
import com.banking.report.service.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 账单与报表控制器
 * Base path: /api/report
 */
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "Report", description = "账单与报表接口（成员E）")
@SecurityRequirement(name = "BearerAuth")
public class ReportController {

    private final ReportService reportService;
    private final UserContext userContext;

    // ===== 1. 分页查询交易历史 =====

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(
            summary = "查询交易历史（分页 + 多条件筛选）",
            description = "支持按交易类型、状态、金额范围、时间范围、关键词筛选，支持按 createdAt/amount 排序")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "403", description = "无权访问该账户"),
            @ApiResponse(responseCode = "404", description = "账户不存在")
    })
    public ResponseEntity<ApiResult<PageResult<TransactionDTO>>> listTransactions(
            @Parameter(description = "账户ID", required = true)
            @PathVariable Long accountId,
            @Valid TransactionQueryRequest req) {

        Long userId = userContext.getUserIdByAccountId(accountId);
        PageResult<TransactionDTO> result = reportService.listTransactions(accountId, req, userId);
        return ResponseEntity.ok(ApiResult.ok(result, "查询成功"));
    }

    // ===== 2. 交易详情 =====

    @GetMapping("/transactions/{transactionNo}")
    @Operation(summary = "查询单条交易详情", description = "根据交易流水号查询详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权查看"),
            @ApiResponse(responseCode = "404", description = "交易不存在")
    })
    public ResponseEntity<ApiResult<TransactionDTO>> getTransactionDetail(
            @Parameter(description = "交易流水号", required = true)
            @PathVariable String transactionNo) {

        Long userId = resolveCurrentUserId();
        TransactionDTO dto = reportService.getTransactionDetail(transactionNo, userId);
        return ResponseEntity.ok(ApiResult.ok(dto, "查询成功"));
    }

    // ===== 3. 账户收支统计 =====

    @GetMapping("/accounts/{accountId}/statistics")
    @Operation(
            summary = "账户收支统计",
            description = "统计指定时间段的总收入、总支出、净值、按类型分组、按天分组明细")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "统计成功"),
            @ApiResponse(responseCode = "400", description = "时间范围不合法"),
            @ApiResponse(responseCode = "403", description = "无权访问")
    })
    public ResponseEntity<ApiResult<AccountStatDTO>> getStatistics(
            @Parameter(description = "账户ID", required = true)
            @PathVariable Long accountId,
            @Parameter(description = "统计开始时间（yyyy-MM-dd HH:mm:ss）", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "统计结束时间（yyyy-MM-dd HH:mm:ss）", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Long userId = userContext.getUserIdByAccountId(accountId);
        AccountStatDTO stat = reportService.getAccountStat(accountId, startTime, endTime, userId);
        return ResponseEntity.ok(ApiResult.ok(stat, "统计成功"));
    }

    // ===== 4. 导出 Excel =====

    @GetMapping("/accounts/{accountId}/export/excel")
    @Operation(
            summary = "导出交易记录为 Excel（.xlsx）",
            description = "流式下载，单次最多 50000 行。时间范围必填，默认最近 3 个月。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "文件流"),
            @ApiResponse(responseCode = "400", description = "导出行数超限或参数错误"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public void exportExcel(
            @PathVariable Long accountId,
            @Valid TransactionQueryRequest req,
            HttpServletResponse response) throws IOException {

        Long userId = userContext.getUserIdByAccountId(accountId);
        reportService.exportExcel(accountId, req, userId, response);
    }

    // ===== 5. 导出 CSV =====

    @GetMapping("/accounts/{accountId}/export/csv")
    @Operation(
            summary = "导出交易记录为 CSV",
            description = "UTF-8 with BOM，兼容 Excel 直接打开中文。单次最多 50000 行。")
    public void exportCsv(
            @PathVariable Long accountId,
            @Valid TransactionQueryRequest req,
            HttpServletResponse response) throws IOException {

        Long userId = userContext.getUserIdByAccountId(accountId);
        reportService.exportCsv(accountId, req, userId, response);
    }

    // ===== 辅助方法 =====

    /**
     * 通过 SecurityContext 用户名，查找对应的 userId
     * （实际项目中 JWT 里直接存 userId 更高效）
     */
    private Long resolveCurrentUserId() {
        // 简化实现：由 controller 的调用方各自传入 accountId 反查，
        // getTransactionDetail 不需要 accountId，这里返回一个占位符让 service 内部二次校验
        // 真实项目中 JWT payload 包含 userId 则直接解析
        return -1L; // service 层会基于 tx 的 fromAccountId/toAccountId 做权限校验
    }
}
