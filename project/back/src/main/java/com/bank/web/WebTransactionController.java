package com.bank.web;

import com.bank.account.config.BankUserDetails;
import com.bank.account.dto.AccountResponse;
import com.bank.account.dto.ApiResponse;
import com.bank.account.entity.Account;
import com.bank.account.enums.AccountStatus;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import com.bank.dto.request.DepositWithdrawRequest;
import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.TransactionResponse;
import com.bank.transaction.dto.DepositRequest;
import com.bank.transaction.dto.WithdrawRequest;
import com.bank.transaction.config.TransactionProperties;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.service.TransactionService;
import com.banking.auth.service.AuthService;
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
    private final AuthService authService;
    private final TransactionProperties transactionProperties;

    @PostMapping("/transfer/send-otp")
    public ApiResponse<String> sendTransferOtp(@AuthenticationPrincipal BankUserDetails user) {
        if (user == null) {
            throw new BusinessException(4010, "请先登录后再发送验证码");
        }
        authService.verifyUserActive(user.getUserId());
        var result = authService.sendTransferOtp(user.getUserId());
        return ApiResponse.success(result.getMessage(), null);
    }

    @PostMapping("/transfer")
    public ApiResponse<TransactionResponse> transfer(
            @AuthenticationPrincipal BankUserDetails user,
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {
        if (user == null) {
            throw new BusinessException(4010, "请先登录");
        }
        authService.verifyUserActive(user.getUserId());
        validateTransferOtpIfRequired(user.getUserId(), request);
        String fromAccountNo = normalizeAccountNo(request.getFromAccountNo());
        AccountResponse fromAccount = accountService.getAccountByNumber(user.getUserId(), fromAccountNo);
        ensureAccountActive(fromAccount.getStatus(), "付款账户");
        Account toAccount = resolveRecipientAccount(request.getToAccountNo());
        ensureAccountActive(toAccount.getStatus(), "收款账户");
        com.bank.transaction.dto.TransferRequest req = new com.bank.transaction.dto.TransferRequest();
        req.setFromAccountId(fromAccount.getId());
        req.setToAccountId(toAccount.getId());
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
        if (user == null) {
            throw new BusinessException(4010, "请先登录");
        }
        authService.verifyUserActive(user.getUserId());
        AccountResponse depositAccount = accountService.getAccountByNumber(
                user.getUserId(), normalizeAccountNo(request.getAccountNo()));
        ensureAccountActive(depositAccount.getStatus(), "存款账户");
        DepositRequest req = new DepositRequest();
        req.setAccountId(depositAccount.getId());
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
        if (user == null) {
            throw new BusinessException(4010, "请先登录");
        }
        authService.verifyUserActive(user.getUserId());
        authService.verifyLoginPassword(user.getUserId(), request.getPassword());
        AccountResponse withdrawAccount = accountService.getAccountByNumber(
                user.getUserId(), normalizeAccountNo(request.getAccountNo()));
        ensureAccountActive(withdrawAccount.getStatus(), "取款账户");
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountId(withdrawAccount.getId());
        req.setAmount(request.getAmount());
        req.setRemark(request.getRemark());
        var result = transactionService.withdraw(req, user.getUserId(), httpRequest.getRemoteAddr());
        return ApiResponse.success("取款成功", toWeb(result));
    }

    private Account resolveRecipientAccount(String toAccountNo) {
        String normalized = normalizeAccountNo(toAccountNo);
        if (normalized.startsWith("TXN")) {
            throw new BusinessException(4005,
                    "收款账户号格式错误：您输入的是交易流水号，请填写以 ACC 开头的对方账户号");
        }
        if (!normalized.startsWith("ACC")) {
            throw new BusinessException(4005,
                    "收款账户号格式错误：账户号应以 ACC 开头，请核对后重试");
        }
        return accountRepository.findByAccountNumber(normalized)
                .orElseThrow(() -> new AccountNotFoundException("收款账户号", normalized));
    }

    private void ensureAccountActive(AccountStatus status, String role) {
        if (status == AccountStatus.ACTIVE) {
            return;
        }
        if (status == AccountStatus.FROZEN) {
            throw new BusinessException(4004, role + "已冻结，无法进行该操作");
        }
        throw new BusinessException(4004, role + "已注销，无法进行该操作");
    }

    private String normalizeAccountNo(String accountNo) {
        return accountNo == null ? "" : accountNo.trim().toUpperCase();
    }

    private void validateTransferOtpIfRequired(Long userId, TransferRequest request) {
        var threshold = transactionProperties.getTransfer().getOtpThreshold();
        if (request.getAmount().compareTo(threshold) <= 0) {
            return;
        }
        String otpCode = request.getOtpCode();
        if (otpCode == null || otpCode.isBlank()) {
            throw new BusinessException(4001,
                    "转账金额超过 " + threshold + " 元，请先获取并填写邮箱验证码");
        }
        authService.verifyTransferOtp(userId, otpCode.trim());
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
