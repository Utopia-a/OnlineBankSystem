package com.bank.account.controller;

import com.bank.account.dto.ApiResponse;
import com.bank.account.dto.InternalBalanceUpdateRequest;
import com.bank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 内部服务接口（仅供成员 D 交易服务调用）
 * 实际部署时建议配合网关 IP 白名单限制
 */
@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "内部接口（交易服务专用）", description = "供成员 D 交易服务调用，外部不可访问")
@PreAuthorize("hasRole('SERVICE')")
public class InternalAccountController {

    private final AccountService accountService;

    @PatchMapping("/balance")
    @Operation(summary = "更新账户余额（内部调用）",
               description = "由成员 D 交易服务在完成转账/存款/取款后调用，更新账户余额")
    public ResponseEntity<ApiResponse<Void>> updateBalance(
            @Valid @RequestBody InternalBalanceUpdateRequest request) {

        log.info("[内部] 余额更新请求 - 账户: {}, 操作: {}, 金额: {}",
                request.getAccountNumber(), request.getOperationType(), request.getAmount());
        accountService.updateBalanceInternal(request);
        return ResponseEntity.ok(ApiResponse.success("余额更新成功", null));
    }

    @GetMapping("/validate")
    @Operation(summary = "验证账户交易可行性（内部调用）",
               description = "在发起交易前，验证账户是否活跃且余额充足")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateAccount(
            @RequestParam String accountNumber,
            @RequestParam(required = false) BigDecimal amount) {

        boolean valid = accountService.validateAccountForTransaction(accountNumber, amount);
        Map<String, Object> result = Map.of(
                "accountNumber", accountNumber,
                "valid", valid
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
