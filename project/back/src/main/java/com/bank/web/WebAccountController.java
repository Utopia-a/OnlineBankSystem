package com.bank.web;

import com.bank.account.config.BankUserDetails;
import com.bank.account.dto.ApiResponse;
import com.bank.account.dto.CreateAccountRequest;
import com.bank.account.enums.AccountType;
import com.bank.account.service.AccountService;
import com.bank.dto.response.AccountResponse;
import com.bank.transaction.exception.BusinessException;
import com.banking.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class WebAccountController {

    private final AccountService accountService;
    private final AuthService authService;

    @GetMapping("/my")
    public ApiResponse<List<AccountResponse>> getMyAccounts(
            @AuthenticationPrincipal BankUserDetails user) {
        List<AccountResponse> accounts = accountService.getAccountsByUserId(user.getUserId())
                .stream().map(this::toWeb).collect(Collectors.toList());
        return ApiResponse.success(accounts);
    }

    @GetMapping("/{accountNo}")
    public ApiResponse<AccountResponse> getAccount(
            @AuthenticationPrincipal BankUserDetails user,
            @PathVariable String accountNo) {
        return ApiResponse.success(toWeb(
                accountService.getAccountByNumber(user.getUserId(), accountNo)));
    }

    @GetMapping("/{accountNo}/balance")
    public ApiResponse<AccountResponse> getBalance(
            @AuthenticationPrincipal BankUserDetails user,
            @PathVariable String accountNo) {
        var balance = accountService.getBalance(user.getUserId(), accountNo);
        AccountResponse resp = toWeb(accountService.getAccountByNumber(user.getUserId(), accountNo));
        resp.setBalance(balance.getBalance());
        return ApiResponse.success(resp);
    }

    @PostMapping
    public ApiResponse<AccountResponse> createAccount(
            @AuthenticationPrincipal BankUserDetails user,
            @RequestParam String type) {
        if (user == null) {
            throw new BusinessException(4010, "请先登录");
        }
        authService.verifyUserActive(user.getUserId());
        CreateAccountRequest req = new CreateAccountRequest();
        req.setAccountType(AccountType.valueOf(type));
        return ApiResponse.success("开户成功",
                toWeb(accountService.createAccount(user.getUserId(), req)));
    }

    private AccountResponse toWeb(com.bank.account.dto.AccountResponse src) {
        AccountResponse resp = new AccountResponse();
        resp.setAccountId(src.getId());
        resp.setAccountNo(src.getAccountNumber());
        resp.setAccountType(src.getAccountType() != null ? src.getAccountType().name() : null);
        resp.setBalance(src.getBalance());
        resp.setStatus(src.getStatus() != null ? src.getStatus().name() : null);
        resp.setCurrency(src.getCurrency());
        resp.setCreateTime(src.getCreatedAt());
        return resp;
    }
}
