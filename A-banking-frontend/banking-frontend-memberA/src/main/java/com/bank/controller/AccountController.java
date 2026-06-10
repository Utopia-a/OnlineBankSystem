package com.bank.controller;

import com.bank.client.AccountServiceClient;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户 API 代理控制器
 * 转发至成员C的账户管理服务（端口8082）
 */
@Tag(name = "账户管理", description = "账户查询、余额查询、开户等接口（对接成员C）")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Operation(summary = "查询我的所有账户", description = "获取当前登录用户名下的所有银行账户列表")
    @GetMapping("/my")
    public ApiResponse<List<AccountResponse>> getMyAccounts(HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        List<AccountResponse> accounts = accountServiceClient.getMyAccounts(token);
        return ApiResponse.success(accounts);
    }

    @Operation(summary = "查询账户详情", description = "根据账户号查询账户详细信息")
    @GetMapping("/{accountNo}")
    public ApiResponse<AccountResponse> getAccount(
            @Parameter(description = "账户号", required = true, example = "6228480000000001")
            @PathVariable String accountNo,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        AccountResponse account = accountServiceClient.getAccountByNo(accountNo, token);
        return ApiResponse.success(account);
    }

    @Operation(summary = "查询账户余额", description = "快速查询指定账户的当前余额")
    @GetMapping("/{accountNo}/balance")
    public ApiResponse<AccountResponse> getBalance(
            @Parameter(description = "账户号", required = true, example = "6228480000000001")
            @PathVariable String accountNo,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        AccountResponse account = accountServiceClient.getBalance(accountNo, token);
        return ApiResponse.success(account);
    }

    @Operation(summary = "开户", description = "为当前用户新增一个银行账户，支持储蓄账户(SAVINGS)和活期账户(CHECKING)")
    @PostMapping
    public ApiResponse<AccountResponse> createAccount(
            @Parameter(description = "账户类型：SAVINGS 或 CHECKING", required = true, example = "SAVINGS")
            @RequestParam String type,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        AccountResponse account = accountServiceClient.createAccount(type, token);
        return ApiResponse.success("开户成功", account);
    }
}
