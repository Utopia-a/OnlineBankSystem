package com.banking.report.dto;

import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 账单与报表模块响应 DTO 集合
 */
public class ReportResponse {

    // ===== 统一响应包装 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResult<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        public static <T> ApiResult<T> ok(T data, String message) {
            return ApiResult.<T>builder()
                    .success(true).message(message)
                    .data(data).timestamp(LocalDateTime.now()).build();
        }

        public static <T> ApiResult<T> fail(String message) {
            return ApiResult.<T>builder()
                    .success(false).message(message)
                    .timestamp(LocalDateTime.now()).build();
        }
    }

    // ===== 分页结果包装 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResult<T> {
        private List<T> records;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    // ===== 单条交易记录 DTO =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDTO {
        private Long id;
        private String transactionNo;
        private Long fromAccountId;
        private String fromAccountNo;   // 关联账户号（展示用）
        private Long toAccountId;
        private String toAccountNo;     // 关联账户号（展示用）
        private BigDecimal amount;
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;
        private String transactionType;
        private String transactionTypeLabel; // 中文标签
        private String status;
        private String statusLabel;
        private String remark;
        private LocalDateTime createdAt;

        public static TransactionDTO from(Transaction t) {
            return fromForAccount(t, null);
        }

        /**
         * 按当前查询账户视角转换交易记录（收款方看到的转账应为 TRANSFER_IN / 入账）
         */
        public static TransactionDTO fromForAccount(Transaction t, Long accountId) {
            TransactionType displayType = t.getTransactionType();
            BigDecimal balanceBefore = t.getFromBalanceBefore() != null
                    ? t.getFromBalanceBefore() : t.getToBalanceBefore();
            BigDecimal balanceAfter = t.getFromBalanceAfter() != null
                    ? t.getFromBalanceAfter() : t.getToBalanceAfter();

            if (accountId != null && t.getTransactionType() == TransactionType.TRANSFER_OUT) {
                if (accountId.equals(t.getToAccountId())) {
                    displayType = TransactionType.TRANSFER_IN;
                    balanceBefore = t.getToBalanceBefore();
                    balanceAfter = t.getToBalanceAfter();
                } else if (accountId.equals(t.getFromAccountId())) {
                    balanceBefore = t.getFromBalanceBefore();
                    balanceAfter = t.getFromBalanceAfter();
                }
            } else if (t.getTransactionType() == TransactionType.DEPOSIT) {
                balanceBefore = t.getToBalanceBefore();
                balanceAfter = t.getToBalanceAfter();
            } else if (t.getTransactionType() == TransactionType.WITHDRAWAL) {
                balanceBefore = t.getFromBalanceBefore();
                balanceAfter = t.getFromBalanceAfter();
            } else if (t.getTransactionType() == TransactionType.TRANSFER_IN) {
                balanceBefore = t.getToBalanceBefore();
                balanceAfter = t.getToBalanceAfter();
            }

            return TransactionDTO.builder()
                    .id(t.getId())
                    .transactionNo(t.getTransactionNo())
                    .fromAccountId(t.getFromAccountId())
                    .toAccountId(t.getToAccountId())
                    .amount(t.getAmount())
                    .balanceBefore(balanceBefore)
                    .balanceAfter(balanceAfter)
                    .transactionType(displayType != null ? displayType.name() : null)
                    .transactionTypeLabel(labelType(displayType))
                    .status(t.getStatus() != null ? t.getStatus().name() : null)
                    .statusLabel(labelStatus(t.getStatus()))
                    .remark(t.getRemark())
                    .createdAt(t.getCreatedAt())
                    .build();
        }

        private static String labelType(TransactionType type) {
            if (type == null) return "";
            return switch (type) {
                case DEPOSIT       -> "存款";
                case WITHDRAWAL    -> "取款";
                case TRANSFER_OUT, TRANSFER_IN -> "转账";
            };
        }

        private static String labelStatus(TransactionStatus status) {
            if (status == null) return "";
            return switch (status) {
                case SUCCESS     -> "成功";
                case FAILED      -> "失败";
                case PENDING     -> "处理中";
                case ROLLED_BACK -> "已回滚";
                case CANCELLED   -> "已撤销";
            };
        }
    }

    // ===== 账户收支统计摘要 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountStatDTO {
        private Long accountId;
        private String accountNo;
        private BigDecimal totalIncome;      // 总收入
        private BigDecimal totalExpense;     // 总支出
        private BigDecimal netAmount;        // 净值 = 收入 - 支出
        private long transactionCount;      // 总笔数
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<TypeStatItem> typeBreakdown;   // 按类型明细
        private List<DayStatItem> dailyBreakdown;   // 按天明细
    }

    // ===== 按类型统计 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStatItem {
        private String transactionType;
        private String label;
        private long count;
        private BigDecimal totalAmount;
    }

    // ===== 按天统计 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayStatItem {
        private String date;        // yyyy-MM-dd
        private long count;
        private BigDecimal totalAmount;
    }

    // ===== 导出任务响应 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportInfo {
        private String filename;
        private String format;
        private long rowCount;
        private String message;
    }
}
