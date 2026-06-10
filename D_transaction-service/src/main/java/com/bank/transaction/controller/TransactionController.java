package com.bank.transaction.controller;

import com.bank.transaction.dto.*;
import com.bank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 交易控制器
 * Base URL: /api/v1/transactions
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "交易服务", description = "提供转账、存款、取款接口")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 从请求中获取操作人ID（由 JWT 过滤器写入）
     */
    private Long getOperatorId(HttpServletRequest request) {
        Object userId = request.getAttribute("currentUserId");
        return userId != null ? (Long) userId : null;
    }

    /**
     * 获取客户端真实 IP（兼容反向代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // ─────────────────────────────────────────────────────
    //  转账
    // ─────────────────────────────────────────────────────

    @PostMapping("/transfer")
    @Operation(
            summary = "转账",
            description = """
                    账户间转账。
                    - 采用悲观锁防止并发超扣
                    - 有单笔限额与单日累计限额双重控制
                    - 付款账户须处于 ACTIVE 状态
                    """
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {

        TransactionResponse result = transactionService.transfer(
                request,
                getOperatorId(httpRequest),
                getClientIp(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("转账成功", result));
    }

    // ─────────────────────────────────────────────────────
    //  存款
    // ─────────────────────────────────────────────────────

    @PostMapping("/deposit")
    @Operation(
            summary = "存款",
            description = """
                    向账户存入资金。
                    - 账户须处于 ACTIVE 状态
                    - 单笔存款有上限控制
                    """
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request,
            HttpServletRequest httpRequest) {

        TransactionResponse result = transactionService.deposit(
                request,
                getOperatorId(httpRequest),
                getClientIp(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("存款成功", result));
    }

    // ─────────────────────────────────────────────────────
    //  取款
    // ─────────────────────────────────────────────────────

    @PostMapping("/withdraw")
    @Operation(
            summary = "取款",
            description = """
                    从账户取出资金。
                    - 余额不足时拒绝
                    - 有单笔限额与单日累计限额双重控制
                    - 账户须处于 ACTIVE 状态
                    """
    )
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            HttpServletRequest httpRequest) {

        TransactionResponse result = transactionService.withdraw(
                request,
                getOperatorId(httpRequest),
                getClientIp(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("取款成功", result));
    }
}
