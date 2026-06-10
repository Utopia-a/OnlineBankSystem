package com.bank.admin.controller;

import com.bank.admin.dto.request.UpdateUserStatusRequest;
import com.bank.admin.dto.request.UserQueryRequest;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.Result;
import com.bank.admin.dto.response.UserVO;
import com.bank.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理员-用户管理", description = "管理员对用户账号的查询与管理操作")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "分页查询用户列表", description = "支持按关键词、状态、角色筛选")
    @GetMapping
    public Result<PageResult<UserVO>> listUsers(UserQueryRequest request) {
        return Result.success(adminUserService.listUsers(request));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(adminUserService.getUserById(userId));
    }

    @Operation(summary = "修改用户状态", description = "支持 ACTIVE / LOCKED(FROZEN) / DISABLED / PENDING_VERIFY")
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        adminUserService.updateUserStatus(userId, request);
        return Result.success();
    }

    @Operation(summary = "重置用户密码", description = "随机生成新密码并返回，需由管理员告知用户")
    @PostMapping("/{userId}/reset-password")
    public Result<Map<String, String>> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        String newPassword = adminUserService.resetUserPassword(userId);
        return Result.success(Map.of("newPassword", newPassword));
    }

    @Operation(summary = "禁用用户", description = "将用户状态置为DISABLED，不可登录")
    @DeleteMapping("/{userId}")
    public Result<Void> disableUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        adminUserService.disableUser(userId);
        return Result.success();
    }
}
