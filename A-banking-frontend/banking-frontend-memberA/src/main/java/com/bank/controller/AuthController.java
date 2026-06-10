package com.bank.controller;

import com.bank.client.AuthServiceClient;
import com.bank.dto.request.LoginRequest;
import com.bank.dto.request.RegisterRequest;
import com.bank.dto.response.ApiResponse;
import com.bank.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 API 代理控制器
 * 前端调用本模块接口，本模块转发至成员B的认证服务（端口8081）
 */
@Tag(name = "认证管理", description = "用户注册、登录、Token刷新、OTP验证码、退出登录接口（对接成员B）")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthServiceClient authServiceClient;

    @Operation(
            summary = "用户登录",
            description = "使用用户名+密码登录，若开启双因素认证需同时传入OTP验证码。登录成功返回 JWT Token。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名或密码错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "OTP验证码错误或过期")
    })
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authServiceClient.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @Operation(
            summary = "用户注册",
            description = "新用户注册，需填写用户名、密码、真实姓名、手机号和身份证号"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数校验失败或用户名已存在")
    })
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        String result = authServiceClient.register(request);
        return ApiResponse.success("注册成功", result);
    }

    @Operation(
            summary = "刷新 Token",
            description = "使用 Refresh Token 换取新的 Access Token，避免用户频繁重新登录"
    )
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @Parameter(description = "Refresh Token", required = true)
            @RequestParam String refreshToken) {
        LoginResponse response = authServiceClient.refreshToken(refreshToken);
        return ApiResponse.success("Token刷新成功", response);
    }

    @Operation(
            summary = "发送 OTP 验证码",
            description = "向指定手机号发送6位OTP验证码，有效期5分钟",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/otp/send")
    public ApiResponse<String> sendOtp(
            @Parameter(description = "手机号", required = true, example = "13812345678")
            @RequestParam String phone,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        authServiceClient.sendOtp(phone, token);
        return ApiResponse.success("验证码已发送", null);
    }

    @Operation(
            summary = "验证 OTP 验证码",
            description = "校验用户输入的OTP验证码是否正确",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/otp/verify")
    public ApiResponse<Boolean> verifyOtp(
            @Parameter(description = "手机号", required = true) @RequestParam String phone,
            @Parameter(description = "6位OTP验证码", required = true) @RequestParam String code,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        Boolean result = authServiceClient.verifyOtp(phone, code, token);
        return ApiResponse.success("验证成功", result);
    }

    @Operation(
            summary = "退出登录",
            description = "使当前 JWT Token 失效，服务端加入黑名单",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        authServiceClient.logout(token);
        return ApiResponse.success("已退出登录", null);
    }
}
