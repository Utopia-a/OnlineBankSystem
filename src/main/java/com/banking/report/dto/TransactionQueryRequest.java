package com.banking.report.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易历史查询请求参数（支持多条件组合、分页、排序）
 */
@Data
public class TransactionQueryRequest {

    // ===== 分页参数 =====

    @Min(value = 0, message = "页码不能为负数")
    private int page = 0;

    @Min(value = 1, message = "每页条数最少 1 条")
    @Max(value = 200, message = "每页条数最多 200 条")
    private int size = 20;

    /** 排序字段（createdAt / amount） */
    private String sortBy = "createdAt";

    /** 排序方向（DESC / ASC） */
    private String sortDir = "DESC";

    // ===== 筛选条件 =====

    /** 交易类型（DEPOSIT / WITHDRAWAL / TRANSFER） */
    private String transactionType;

    /** 交易状态（SUCCESS / FAILED / PENDING） */
    private String status;

    /** 开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 最小金额 */
    @DecimalMin(value = "0.01", message = "最小金额需大于 0")
    private BigDecimal minAmount;

    /** 最大金额 */
    private BigDecimal maxAmount;

    /** 关键词（模糊匹配流水号、备注） */
    @Size(max = 50, message = "关键词最多 50 字符")
    private String keyword;

    // ===== 导出参数 =====

    /** 导出格式（excel / csv） */
    private String exportFormat;
}
