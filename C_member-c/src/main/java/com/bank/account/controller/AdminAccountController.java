package com.bank.account.controller;

import com.bank.account.dto.AccountResponse;
import com.bank.account.dto.AccountStatusRequest;
import com.bank.account.dto.ApiResponse;
import com.bank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员账户管理接口（供成员 F 管理后台使用）
 */
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@Tag(name = "管理员账户管理", description = "管理员对所有账户的管控操作")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "分页查询所有账户", description = "管理员查看系统全部账户")
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAllAccounts(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AccountResponse> accounts = accountService.adminGetAllAccounts(pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询指定用户的账户", description = "根据用户ID查询其所有账户")
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAccountsByUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AccountResponse> accounts = accountService.adminGetAccountsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PatchMapping("/{accountId}/status")
    @Operation(summary = "管理员变更账户状态",
               description = "管理员可执行任意状态变更，包括注销账户")
    public ResponseEntity<ApiResponse<AccountResponse>> adminChangeStatus(
            @Parameter(description = "账户ID") @PathVariable Long accountId,
            @Valid @RequestBody AccountStatusRequest request) {

        AccountResponse response = accountService.adminChangeAccountStatus(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("账户状态已变更", response));
    }
}
