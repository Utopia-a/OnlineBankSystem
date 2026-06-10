package com.banking.report.service;

import com.bank.account.config.BankUserDetails;
import com.bank.account.repository.AccountRepository;
import com.banking.report.exception.ReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final AccountRepository accountRepository;

    public Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ReportException.AccessDeniedException("未登录");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof BankUserDetails details) {
            return details.getUserId();
        }
        throw new ReportException.AccessDeniedException("无法获取用户信息");
    }

    public Long getUserIdByAccountId(Long accountId) {
        return accountRepository.findById(accountId)
                .map(a -> a.getUserId())
                .orElseThrow(() -> new ReportException.AccountNotFoundException(
                        "账户不存在: " + accountId));
    }
}
