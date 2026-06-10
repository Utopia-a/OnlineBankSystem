package com.banking.auth.controller;

import com.banking.auth.dto.AuthRequest;
import com.banking.auth.dto.AuthResponse.*;
import com.banking.auth.entity.OtpRecord.OtpType;
import com.banking.auth.entity.User;
import com.banking.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证与安全控制器
 * Base path: /api/auth
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "认证与安全接口（成员B）")
public class AuthController {

    private final AuthService authService;

    // ===== 注册 =====

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册后发送邮箱验证 OTP，需完成验证才能登录")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "409", description = "用户名/邮箱已存在")
    })
    public ResponseEntity<ApiResult<RegisterResult>> register(
            @Valid @RequestBody AuthRequest.Register request) {
        RegisterResult result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.ok(result, "注册成功"));
    }

    // ===== 邮箱验证 =====

    @PostMapping("/verify-email")
    @Operation(summary = "邮箱验证", description = "使用注册时收到的 6 位 OTP 完成邮箱验证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "验证成功"),
            @ApiResponse(responseCode = "400", description = "OTP 无效或已过期")
    })
    public ResponseEntity<ApiResult<Void>> verifyEmail(
            @Valid @RequestBody AuthRequest.VerifyOtp request) {
        authService.verifyEmail(request.getTarget(), request.getCode());
        return ResponseEntity.ok(ApiResult.ok(null, "邮箱验证成功，可以登录了"));
    }

    // ===== 登录 =====

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "返回 accessToken 和 refreshToken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "423", description = "账户已锁定"),
            @ApiResponse(responseCode = "403", description = "邮箱未验证")
    })
    public ResponseEntity<ApiResult<LoginResult>> login(
            @Valid @RequestBody AuthRequest.Login request,
            HttpServletRequest httpRequest) {
        String clientInfo = httpRequest.getHeader("User-Agent");
        LoginResult result = authService.login(request, clientInfo);
        return ResponseEntity.ok(ApiResult.ok(result, "登录成功"));
    }

    // ===== 刷新 Token =====

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新 Token", description = "使用 refreshToken 获取新的 accessToken（旋转机制）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "刷新成功"),
            @ApiResponse(responseCode = "401", description = "refreshToken 无效或已过期")
    })
    public ResponseEntity<ApiResult<TokenResult>> refreshToken(
            @Valid @RequestBody AuthRequest.RefreshToken request,
            HttpServletRequest httpRequest) {
        String clientInfo = httpRequest.getHeader("User-Agent");
        TokenResult result = authService.refreshToken(request.getRefreshToken(), clientInfo);
        return ResponseEntity.ok(ApiResult.ok(result, "Token 刷新成功"));
    }

    // ===== 登出 =====

    @PostMapping("/logout")
    @Operation(summary = "登出（当前设备）", description = "撤销当前 refreshToken",
            security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResult<Void>> logout(
            @Valid @RequestBody AuthRequest.RefreshToken request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResult.ok(null, "已成功登出"));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "登出所有设备", description = "撤销该用户所有 refreshToken",
            security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResult<Void>> logoutAll(
            @AuthenticationPrincipal User user) {
        authService.logoutAll(user.getId());
        return ResponseEntity.ok(ApiResult.ok(null, "已从所有设备登出"));
    }

    // ===== 修改密码 =====

    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "需要提供旧密码，修改成功后所有设备下线",
            security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "401", description = "旧密码错误")
    })
    public ResponseEntity<ApiResult<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AuthRequest.ChangePassword request) {
        authService.changePassword(user.getId(), request);
        return ResponseEntity.ok(ApiResult.ok(null, "密码修改成功，请重新登录"));
    }

    // ===== 找回密码 =====

    @PostMapping("/forgot-password")
    @Operation(summary = "找回密码：发送 OTP", description = "向注册邮箱发送密码重置验证码")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "404", description = "邮箱未注册"),
            @ApiResponse(responseCode = "429", description = "请求过于频繁")
    })
    public ResponseEntity<ApiResult<OtpResult>> forgotPassword(
            @Valid @RequestBody AuthRequest.ForgotPassword request) {
        OtpResult result = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResult.ok(result, "密码重置验证码已发送"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "找回密码：重置密码", description = "使用 OTP 验证后设置新密码")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "重置成功"),
            @ApiResponse(responseCode = "400", description = "OTP 无效或已过期")
    })
    public ResponseEntity<ApiResult<Void>> resetPassword(
            @Valid @RequestBody AuthRequest.ResetPassword request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResult.ok(null, "密码重置成功，请重新登录"));
    }

    // ===== 重发 OTP =====

    @PostMapping("/resend-otp")
    @Operation(summary = "重发 OTP", description = "重新发送指定类型的验证码（有频率限制）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "429", description = "发送过于频繁，请稍后重试")
    })
    public ResponseEntity<ApiResult<OtpResult>> resendOtp(
            @Valid @RequestBody AuthRequest.ResendOtp request) {
        OtpType type = OtpType.valueOf(request.getType());
        OtpResult result = authService.resendOtp(request.getTarget(), type);
        return ResponseEntity.ok(ApiResult.ok(result, "验证码已重新发送"));
    }

    // ===== 获取当前用户信息 =====

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户信息",
            security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResult<UserInfo>> getCurrentUser(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResult.ok(UserInfo.from(user), "获取成功"));
    }
}
