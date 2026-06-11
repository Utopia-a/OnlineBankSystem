package com.bank.web;

import com.bank.account.config.BankUserDetails;
import com.bank.account.dto.ApiResponse;
import com.bank.account.service.AccountService;
import com.bank.dto.response.PageResponse;
import com.bank.dto.response.TransactionResponse;
import com.banking.report.dto.ReportResponse.PageResult;
import com.banking.report.dto.ReportResponse.TransactionDTO;
import com.banking.report.dto.TransactionQueryRequest;
import com.banking.report.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class WebBillController {

    private final ReportService reportService;
    private final AccountService accountService;

    @GetMapping("/history")
    public ApiResponse<PageResponse<TransactionResponse>> getHistory(
            @AuthenticationPrincipal BankUserDetails user,
            @RequestParam String accountNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate) {

        Long accountId = accountService.getAccountByNumber(user.getUserId(), accountNo).getId();
        TransactionQueryRequest req = new TransactionQueryRequest();
        req.setPage(Math.max(0, page - 1));
        req.setSize(pageSize);
        if (startDate != null) req.setStartTime(LocalDate.parse(startDate).atStartOfDay());
        if (endDate != null) req.setEndTime(LocalDate.parse(endDate).atTime(LocalTime.MAX));

        PageResult<TransactionDTO> result = reportService.listTransactions(
                accountId, req, user.getUserId());

        PageResponse<TransactionResponse> resp = new PageResponse<>();
        resp.setPage(page);
        resp.setPageSize(pageSize);
        resp.setTotal(result.getTotalElements());
        resp.setTotalPages(result.getTotalPages());
        resp.setRecords(result.getRecords().stream().map(this::toWeb).collect(Collectors.toList()));
        return ApiResponse.success(resp);
    }

    @GetMapping("/export")
    public void export(
            @AuthenticationPrincipal BankUserDetails user,
            @RequestParam String accountNo,
            @RequestParam(defaultValue = "CSV") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {

        Long accountId = accountService.getAccountByNumber(user.getUserId(), accountNo).getId();
        TransactionQueryRequest req = new TransactionQueryRequest();
        if (startDate != null) req.setStartTime(LocalDate.parse(startDate).atStartOfDay());
        if (endDate != null) req.setEndTime(LocalDate.parse(endDate).atTime(LocalTime.MAX));

        if ("CSV".equalsIgnoreCase(format)) {
            reportService.exportCsv(accountId, req, user.getUserId(), response);
        } else {
            reportService.exportExcel(accountId, req, user.getUserId(), response);
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"transactions_" + accountNo + "." + format.toLowerCase() + "\"");
    }

    private TransactionResponse toWeb(TransactionDTO dto) {
        TransactionResponse resp = new TransactionResponse();
        resp.setTransactionId(dto.getTransactionNo());
        resp.setFromAccountNo(dto.getFromAccountNo());
        resp.setToAccountNo(dto.getToAccountNo());
        resp.setAmount(dto.getAmount());
        resp.setTransactionType(dto.getTransactionType());
        resp.setStatus(dto.getStatus());
        resp.setRemark(dto.getRemark());
        resp.setTransactionTime(dto.getCreatedAt());
        return resp;
    }
}
