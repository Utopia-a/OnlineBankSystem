package com.bank.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端页面路由控制器（Thymeleaf 模板渲染）
 * 注解 @Hidden 使该 Controller 不出现在 Swagger API 文档中
 */
@Hidden
@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/verify-email")
    public String verifyEmailPage() {
        return "verify-email";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/accounts")
    public String accountsPage() {
        return "accounts";
    }

    @GetMapping("/transfer")
    public String transferPage() {
        return "transfer";
    }

    @GetMapping("/transactions")
    public String transactionsPage() {
        return "transactions";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage() {
        return "admin/users";
    }

    @GetMapping("/admin/accounts")
    public String adminAccountsPage() {
        return "admin/accounts";
    }

    @GetMapping("/admin/config")
    public String adminConfigPage() {
        return "admin/config";
    }

    @GetMapping("/admin/transactions")
    public String adminTransactionsPage() {
        return "admin/transactions";
    }

    @GetMapping("/admin/operation-logs")
    public String adminOperationLogsPage() {
        return "admin/operation-logs";
    }

    @GetMapping("/admin/admins")
    public String adminAdminsPage() {
        return "admin/admins";
    }
}
