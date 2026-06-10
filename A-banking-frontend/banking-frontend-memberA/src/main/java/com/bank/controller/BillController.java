package com.bank.controller;

import com.bank.client.BillServiceClient;
import com.bank.dto.response.ApiResponse;
import com.bank.dto.response.PageResponse;
import com.bank.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 账单 & 报表 API 代理控制器
 * 转发至成员E的账单服务（端口8084）
 */
@Tag(name = "账单与报表", description = "交易历史查询、分页、导出功能（对接成员E）")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private static final Logger log = LoggerFactory.getLogger(BillController.class);

    @Autowired
    private BillServiceClient billServiceClient;

    @Operation(
            summary = "查询交易历史（分页）",
            description = """
                    分页查询指定账户的交易历史记录，支持按日期范围过滤。
                    - page 从 1 开始
                    - pageSize 默认 10，最大 100
                    - startDate / endDate 格式：yyyy-MM-dd
                    """
    )
    @GetMapping("/history")
    public ApiResponse<PageResponse<TransactionResponse>> getHistory(
            @Parameter(description = "账户号", required = true, example = "6228480000000001")
            @RequestParam String accountNo,

            @Parameter(description = "页码（从1开始）", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "开始日期（yyyy-MM-dd）", example = "2024-01-01")
            @RequestParam(required = false) String startDate,

            @Parameter(description = "结束日期（yyyy-MM-dd）", example = "2024-12-31")
            @RequestParam(required = false) String endDate,

            HttpServletRequest request) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 10;

        String token = (String) request.getAttribute("jwtToken");
        PageResponse<TransactionResponse> result = billServiceClient.getTransactionHistory(
                accountNo, page, pageSize, startDate, endDate, token);
        return ApiResponse.success(result);
    }

    @Operation(
            summary = "导出交易记录",
            description = """
                    将指定账户的交易记录导出为文件下载。
                    - format=CSV：导出为 CSV 格式（可用 Excel 打开）
                    - format=PDF：导出为 PDF 报表
                    """
    )
    @GetMapping("/export")
    public void exportTransactions(
            @Parameter(description = "账户号", required = true, example = "6228480000000001")
            @RequestParam String accountNo,

            @Parameter(description = "导出格式：CSV 或 PDF", example = "CSV")
            @RequestParam(defaultValue = "CSV") String format,

            @Parameter(description = "开始日期（yyyy-MM-dd）")
            @RequestParam(required = false) String startDate,

            @Parameter(description = "结束日期（yyyy-MM-dd）")
            @RequestParam(required = false) String endDate,

            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String token = (String) request.getAttribute("jwtToken");
        byte[] fileData = billServiceClient.exportTransactions(
                accountNo, startDate, endDate, format, token);

        String filename = "transactions_" + accountNo + "." + format.toLowerCase();
        String contentType = "CSV".equalsIgnoreCase(format)
                ? "text/csv;charset=UTF-8"
                : "application/pdf";

        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");
        response.setContentLength(fileData != null ? fileData.length : 0);

        if (fileData != null) {
            response.getOutputStream().write(fileData);
            response.getOutputStream().flush();
        }
        log.info("导出交易记录完成: 账户 {}, 格式 {}, 大小 {} bytes",
                accountNo, format, fileData != null ? fileData.length : 0);
    }
}
