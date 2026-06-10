package com.bank.admin.controller;

import com.bank.account.config.BankUserDetails;
import com.bank.admin.dto.request.SystemConfigRequest;
import com.bank.admin.dto.response.Result;
import com.bank.admin.entity.SystemConfig;
import com.bank.admin.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员-系统配置", description = "管理系统全局配置项")
@RestController
@RequestMapping("/api/admin/configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取所有配置项")
    @GetMapping
    public Result<List<SystemConfig>> listAll() {
        return Result.success(systemConfigService.listAll());
    }

    @Operation(summary = "根据key查询配置项")
    @GetMapping("/key/{configKey}")
    public Result<SystemConfig> getByKey(
            @Parameter(description = "配置键") @PathVariable String configKey) {
        return Result.success(systemConfigService.getByKey(configKey));
    }

    @Operation(summary = "新增配置项")
    @PostMapping
    public Result<SystemConfig> create(
            @Valid @RequestBody SystemConfigRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(systemConfigService.create(request, extractUserId(userDetails)));
    }

    @Operation(summary = "更新配置项")
    @PutMapping("/{id}")
    public Result<SystemConfig> update(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Valid @RequestBody SystemConfigRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(systemConfigService.update(id, request, extractUserId(userDetails)));
    }

    @Operation(summary = "删除配置项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "配置ID") @PathVariable Long id) {
        systemConfigService.delete(id);
        return Result.success();
    }

    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof BankUserDetails details) {
            return details.getUserId();
        }
        return null;
    }
}
