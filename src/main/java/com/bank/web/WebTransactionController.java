package com.bank.web;

import com.bank.account.config.BankUserDetails;
import com.bank.account.dto.ApiResponse;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import com.bank.dto.request.DepositWithdrawRequest;
import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.transaction.dto.DepositRequest;
import com.bank.transaction.dto.WithdrawRequest;
import com.bank.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class WebTransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @PostMapping("/transfer")
    public ApiResponse<TransactionResponse> transfer(
            @AuthenticationPrincipal BankUserDetails user,
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {
        com.bank.transaction.dto.TransferRequest req = new com.bank.transaction.dto.TransferRequest();
        req.setFromAccountId(accountService.getAccountByNumber(
                user.getUserId(), request.getFromAccountNo()).getId());
        req.setToAccountId(accountRepository.findByAccountNumber(request.getToAccountNo())
                .orElseThrow(() -> new AccountNotFoundException(request.getToAccountNo()))
                .getId());
        req.setAmount(request.getAmount());
        req.setRemark(request.getRemark());
        var result = transactionService.transfer(req, user.getUserId(), httpRequest.getRemoteAddr());
        return ApiResponse.success("转账成功", toWeb(result));
    }

    @PostMapping("/deposit")
    public ApiResponse<TransactionResponse> deposit(
            @AuthenticationPrincipal BankUserDetails user,
            @Valid @RequestBody DepositWithdrawRequest request,
            HttpServletRequest httpRequest) {
        DepositRequest req = new DepositRequest();
        req.setAccountId(accountService.getAccountByNumber(
                user.getUserId(), request.getAccountNo()).getId());
        req.setAmount(request.getAmount());
        req.setRemark(request.getRemark());
        var result = transactionService.deposit(req, user.getUserId(), httpRequest.getRemoteAddr());
        return ApiResponse.success("存款成功", toWeb(result));
    }

    @PostMapping("/withdraw")
    public ApiResponse<TransactionResponse> withdraw(
            @AuthenticationPrincipal BankUserDetails user,
            @Valid @RequestBody DepositWithdrawRequest request,
            HttpServletRequest httpRequest) {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountId(accountService.getAccountByNumber(
                user.getUserId(), request.getAccountNo()).getId());
        req.setAmount(request.getAmount());
        req.setRemark(request.getRemark());
        var result = transactionService.withdraw(req, user.getUserId(), httpRequest.getRemoteAddr());
        return ApiResponse.success("取款成功", toWeb(result));
    }

    private TransactionResponse toWeb(com.bank.transaction.dto.TransactionResponse src) {
        TransactionResponse resp = new TransactionResponse();
        resp.setTransactionId(src.getTransactionNo());
        resp.setTransactionType(src.getTransactionType() != null ? src.getTransactionType().name() : null);
        resp.setAmount(src.getAmount());
        resp.setStatus(src.getStatus() != null ? src.getStatus().name() : null);
        resp.setRemark(src.getRemark());
        resp.setTransactionTime(src.getCreatedAt());
        return resp;
    }
}
