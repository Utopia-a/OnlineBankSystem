package com.banking.report.service;

import com.banking.report.dto.ReportResponse.*;
import com.banking.report.dto.TransactionQueryRequest;
import com.bank.account.entity.Account;
import com.bank.account.repository.AccountRepository;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import com.banking.report.exception.ReportException;
import com.banking.report.repository.TransactionSpecification;
import com.banking.report.util.ExportUtil;
import com.banking.report.util.PageUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单与报表核心服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ExportUtil exportUtil;

    @Value("${export.max-rows:50000}")
    private int maxExportRows;

    private static final DateTimeFormatter FILENAME_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // ===== 交易历史分页查询 =====

    /**
     * 查询指定账户的交易历史（多条件筛选 + 分页）
     *
     * @param accountId 账户ID
     * @param req       查询条件
     * @param userId    当前登录用户ID（权限校验用）
     */
    @Transactional(readOnly = true)
    public PageResult<TransactionDTO> listTransactions(
            Long accountId, TransactionQueryRequest req, Long userId) {

        // 权限校验：账户必须属于当前用户（管理员跳过）
        validateAccountOwnership(accountId, userId);

        Pageable pageable = PageUtil.buildPageable(req);
        Page<Transaction> page = transactionRepository.findAll(
                TransactionSpecification.buildSpec(req, accountId), pageable);

        List<TransactionDTO> dtos = page.getContent().stream()
                .map(t -> enrichWithAccountNo(TransactionDTO.from(t)))
                .collect(Collectors.toList());

        return PageResult.<TransactionDTO>builder()
                .records(dtos)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    // ===== 单条交易详情 =====

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionDetail(String transactionNo, Long userId) {
        Transaction tx = transactionRepository.findByTransactionNo(transactionNo)
                .orElseThrow(() -> new ReportException.TransactionNotFoundException(
                        "交易记录不存在: " + transactionNo));

        // 权限：发起方或接收方账户属于当前用户
        boolean authorized = isAccountOwner(tx.getFromAccountId(), userId)
                || isAccountOwner(tx.getToAccountId(), userId);
        if (!authorized) {
            throw new ReportException.AccessDeniedException("无权限查看该交易记录");
        }
        return enrichWithAccountNo(TransactionDTO.from(tx));
    }

    // ===== 账户收支统计 =====

    /**
     * 账户在指定时间段内的收支统计摘要
     */
    @Transactional(readOnly = true)
    public AccountStatDTO getAccountStat(Long accountId, LocalDateTime startTime,
                                          LocalDateTime endTime, Long userId) {
        validateAccountOwnership(accountId, userId);
        validateDateRange(startTime, endTime);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ReportException.AccountNotFoundException("账户不存在"));

        BigDecimal totalIncome  = transactionRepository.sumIncome(accountId, startTime, endTime);
        BigDecimal totalExpense = transactionRepository.sumExpense(accountId, startTime, endTime);

        // 按类型分组
        List<Object[]> typeRows = transactionRepository.groupByTypeStats(accountId, startTime, endTime);
        List<TypeStatItem> typeBreakdown = typeRows.stream()
                .map(row -> TypeStatItem.builder()
                        .transactionType(row[0].toString())
                        .label(labelType(row[0].toString()))
                        .count((Long) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());

        // 按天分组
        List<Object[]> dayRows = transactionRepository.groupByDayStats(accountId, startTime, endTime);
        List<DayStatItem> dailyBreakdown = dayRows.stream()
                .map(row -> DayStatItem.builder()
                        .date(row[0].toString())
                        .count((Long) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());

        long txCount = typeBreakdown.stream().mapToLong(TypeStatItem::getCount).sum();

        return AccountStatDTO.builder()
                .accountId(accountId)
                .accountNo(account.getAccountNumber())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netAmount(totalIncome.subtract(totalExpense))
                .transactionCount(txCount)
                .startTime(startTime)
                .endTime(endTime)
                .typeBreakdown(typeBreakdown)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    // ===== 导出 Excel =====

    /**
     * 导出交易记录为 Excel（.xlsx），直接写入 HTTP 响应流
     */
    @Transactional(readOnly = true)
    public ExportInfo exportExcel(Long accountId, TransactionQueryRequest req,
                                   Long userId, HttpServletResponse response) throws IOException {
        validateAccountOwnership(accountId, userId);
        validateDateRange(req.getStartTime(), req.getEndTime());

        List<Transaction> records = fetchForExport(accountId, req);
        List<TransactionDTO> dtos = records.stream()
                .map(t -> enrichWithAccountNo(TransactionDTO.from(t)))
                .collect(Collectors.toList());

        String filename = buildFilename(accountId, "xlsx");
        setExcelResponseHeaders(response, filename);
        exportUtil.writeExcel(dtos, response.getOutputStream(), buildSheetTitle(req));
        response.getOutputStream().flush();

        log.info("Excel 导出 accountId={} rows={}", accountId, dtos.size());
        return ExportInfo.builder()
                .filename(filename)
                .format("excel")
                .rowCount(dtos.size())
                .message("导出成功")
                .build();
    }

    // ===== 导出 CSV =====

    /**
     * 导出交易记录为 CSV，直接写入 HTTP 响应流
     */
    @Transactional(readOnly = true)
    public ExportInfo exportCsv(Long accountId, TransactionQueryRequest req,
                                 Long userId, HttpServletResponse response) throws IOException {
        validateAccountOwnership(accountId, userId);
        validateDateRange(req.getStartTime(), req.getEndTime());

        List<Transaction> records = fetchForExport(accountId, req);
        List<TransactionDTO> dtos = records.stream()
                .map(t -> enrichWithAccountNo(TransactionDTO.from(t)))
                .collect(Collectors.toList());

        String filename = buildFilename(accountId, "csv");
        setCsvResponseHeaders(response, filename);
        exportUtil.writeCsv(dtos, response.getOutputStream());
        response.getOutputStream().flush();

        log.info("CSV 导出 accountId={} rows={}", accountId, dtos.size());
        return ExportInfo.builder()
                .filename(filename)
                .format("csv")
                .rowCount(dtos.size())
                .message("导出成功")
                .build();
    }

    // ===== 私有方法 =====

    private List<Transaction> fetchForExport(Long accountId, TransactionQueryRequest req) {
        // 默认导出最近 3 个月（未指定时间范围时）
        LocalDateTime startTime = req.getStartTime() != null
                ? req.getStartTime() : LocalDateTime.now().minusMonths(3);
        LocalDateTime endTime = req.getEndTime() != null
                ? req.getEndTime() : LocalDateTime.now();

        Pageable limit = PageRequest.of(0, maxExportRows);
        List<Transaction> records = transactionRepository.findForExport(
                accountId, startTime, endTime, limit);

        if (records.size() >= maxExportRows) {
            throw new ReportException.ExportTooLargeException(
                    "导出数据超过 " + maxExportRows + " 行限制，请缩小时间范围后重试");
        }
        return records;
    }

    private void validateAccountOwnership(Long accountId, Long userId) {
        if (accountId == null) return;
        if (!accountRepository.existsByIdAndUserId(accountId, userId)) {
            // 若账户不存在报 404，若存在但不属于当前用户报 403
            Account acc = accountRepository.findById(accountId).orElse(null);
            if (acc == null) {
                throw new ReportException.AccountNotFoundException("账户不存在: " + accountId);
            }
            throw new ReportException.AccessDeniedException("无权限访问该账户");
        }
    }

    private boolean isAccountOwner(Long accountId, Long userId) {
        if (accountId == null) return false;
        return accountRepository.existsByIdAndUserId(accountId, userId);
    }

    private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new ReportException.InvalidQueryException("开始时间不能晚于结束时间");
        }
        // 最大查询跨度 1 年
        if (startTime != null && endTime != null
                && startTime.plusYears(1).isBefore(endTime)) {
            throw new ReportException.InvalidQueryException("查询时间范围不能超过 1 年");
        }
    }

    /**
     * 将账户ID翻译为账户号（批量映射可优化，此处简化）
     */
    private TransactionDTO enrichWithAccountNo(TransactionDTO dto) {
        if (dto.getFromAccountId() != null) {
            accountRepository.findById(dto.getFromAccountId())
                    .ifPresent(a -> dto.setFromAccountNo(a.getAccountNumber()));
        }
        if (dto.getToAccountId() != null) {
            accountRepository.findById(dto.getToAccountId())
                    .ifPresent(a -> dto.setToAccountNo(a.getAccountNumber()));
        }
        return dto;
    }

    private void setExcelResponseHeaders(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    private void setCsvResponseHeaders(HttpServletResponse response, String filename) {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    private String buildFilename(Long accountId, String ext) {
        return "transactions_" + accountId + "_" + LocalDateTime.now().format(FILENAME_FMT) + "." + ext;
    }

    private String buildSheetTitle(TransactionQueryRequest req) {
        String start = req.getStartTime() != null
                ? req.getStartTime().toLocalDate().toString() : "all";
        String end = req.getEndTime() != null
                ? req.getEndTime().toLocalDate().toString() : "now";
        return "交易明细 " + start + " ~ " + end;
    }

    private String labelType(String type) {
        return switch (type) {
            case "DEPOSIT"    -> "存款";
            case "WITHDRAWAL" -> "取款";
            case "TRANSFER", "TRANSFER_OUT", "TRANSFER_IN" -> "转账";
            default -> type;
        };
    }
}
