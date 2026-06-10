package com.banking.report.service;

import com.banking.report.entity.AccountSummary;
import com.banking.report.exception.ReportException;
import com.banking.report.repository.AccountRepository;
import com.banking.report.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户上下文工具
 * 从 SecurityContext 获取当前用户名，再通过账户关联查用户ID
 * （若有用户服务 Feign 可直接查 users 表，此处通过 accounts 反查简化实现）
 */
@Component
@RequiredArgsConstructor
public class UserContext {

    private final AccountRepository accountRepository;

    /**
     * 获取当前登录用户名（来自 JWT subject）
     */
    public String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ReportException.AccessDeniedException("未登录");
        }
        return auth.getName();
    }

    /**
     * 通过账户ID反查所属用户ID（用于权限校验）
     * 注：更好的方式是直接在 JWT 中存 userId，此处演示通过 DB 查找
     */
    public Long getUserIdByAccountId(Long accountId) {
        return accountRepository.findById(accountId)
                .map(AccountSummary::getUserId)
                .orElseThrow(() -> new ReportException.AccountNotFoundException(
                        "账户不存在: " + accountId));
    }
}
