package com.bank.account.controller;

import com.bank.account.config.BankUserDetails;
import com.bank.account.dto.*;
import com.bank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户管理 Controller
 * 处理账户 CRUD 及余额查询
 */
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "账户管理", description = "账户的增删改查及状态管理")
@SecurityRequirement(name = "BearerAuth")
public class AccountController {

    private final AccountService accountService;

    // ─────────────────────────────────────────────────────────────────────────
    // 创建账户
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "创建银行账户", description = "为当前登录用户创建新的银行账户")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "创建成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数不合法"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @AuthenticationPrincipal BankUserDetails userDetails,
            @Valid @RequestBody CreateAccountRequest request) {

        AccountResponse response = accountService.createAccount(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 查询账户
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "查询我的所有账户", description = "查询当前登录用户的所有账户列表")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal BankUserDetails userDetails) {

        List<AccountResponse> accounts = accountService.getAccountsByUserId(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/paged")
    @Operation(summary = "分页查询我的账户")
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getMyAccountsPaged(
            @AuthenticationPrincipal BankUserDetails userDetails,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AccountResponse> accounts = accountService.getAccountsByUserIdPaged(
                userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "按账户ID查询账户详情")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @AuthenticationPrincipal BankUserDetails userDetails,
            @Parameter(description = "账户ID") @PathVariable Long accountId) {

        AccountResponse response = accountService.getAccountById(userDetails.getUserId(), accountId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "按账户号码查询账户详情")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @AuthenticationPrincipal BankUserDetails userDetails,
            @Parameter(description = "账户号码，如 ACC202401010000001") @PathVariable String accountNumber) {

        AccountResponse response = accountService.getAccountByNumber(
                userDetails.getUserId(), accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 余额查询
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/number/{accountNumber}/balance")
    @Operation(summary = "查询账户余额", description = "仅查询余额，不返回完整账户信息")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @AuthenticationPrincipal BankUserDetails userDetails,
            @Parameter(description = "账户号码") @PathVariable String accountNumber) {

        BalanceResponse balance = accountService.getBalance(
                userDetails.getUserId(), accountNumber);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 更新账户
    // ─────────────────────────────────────────────────────────────────────────

    @PutMapping("/{accountId}")
    @Operation(summary = "更新账户信息", description = "更新账户别名、日限额、备注等非核心信息")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @AuthenticationPrincipal BankUserDetails userDetails,
            @Parameter(description = "账户ID") @PathVariable Long accountId,
            @Valid @RequestBody UpdateAccountRequest request) {

        AccountResponse response = accountService.updateAccount(
                userDetails.getUserId(), accountId, request);
        return ResponseEntity.ok(ApiResponse.success("账户信息更新成功", response));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 账户状态管理
    // ─────────────────────────────────────────────────────────────────────────

    @PatchMapping("/{accountId}/status")
    @Operation(summary = "变更账户状态",
               description = "普通用户可冻结/解冻自己的账户（ACTIVE ↔ FROZEN）；注销请联系管理员")
    public ResponseEntity<ApiResponse<AccountResponse>> changeStatus(
            @AuthenticationPrincipal BankUserDetails userDetails,
            @Parameter(description = "账户ID") @PathVariable Long accountId,
            @Valid @RequestBody AccountStatusRequest request) {

        AccountResponse response = accountService.changeAccountStatus(
                userDetails.getUserId(), accountId, request);
        return ResponseEntity.ok(ApiResponse.success("账户状态变更成功", response));
    }
}
