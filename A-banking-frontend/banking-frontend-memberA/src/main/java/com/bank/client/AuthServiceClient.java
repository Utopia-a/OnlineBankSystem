package com.bank.client;

import com.bank.dto.request.LoginRequest;
import com.bank.dto.request.RegisterRequest;
import com.bank.dto.response.ApiResponse;
import com.bank.dto.response.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * 认证服务客户端（对接成员B - 认证 & 安全服务）
 * 端口：8081
 */
@Component
public class AuthServiceClient extends BaseServiceClient {

    @Value("${backend.auth-service.url}")
    private String authServiceUrl;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        String url = authServiceUrl + "/api/auth/login";
        log.info("调用认证服务登录: {}", request.getUsername());
        return post(url, request, null,
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {});
    }

    /**
     * 用户注册
     */
    public String register(RegisterRequest request) {
        String url = authServiceUrl + "/api/auth/register";
        log.info("调用认证服务注册: {}", request.getUsername());
        return post(url, request, null,
                new ParameterizedTypeReference<ApiResponse<String>>() {});
    }

    /**
     * 刷新 Token
     */
    public LoginResponse refreshToken(String refreshToken) {
        String url = authServiceUrl + "/api/auth/refresh";
        log.info("调用认证服务刷新 Token");
        return post(url, refreshToken, null,
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {});
    }

    /**
     * 发送 OTP 验证码
     */
    public String sendOtp(String phone, String token) {
        String url = authServiceUrl + "/api/auth/otp/send?phone=" + phone;
        log.info("调用认证服务发送OTP: {}", phone);
        return post(url, null, token,
                new ParameterizedTypeReference<ApiResponse<String>>() {});
    }

    /**
     * 验证 OTP 验证码
     */
    public Boolean verifyOtp(String phone, String code, String token) {
        String url = authServiceUrl + "/api/auth/otp/verify?phone=" + phone + "&code=" + code;
        log.info("调用认证服务验证OTP: {}", phone);
        return post(url, null, token,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {});
    }

    /**
     * 退出登录
     */
    public String logout(String token) {
        String url = authServiceUrl + "/api/auth/logout";
        log.info("调用认证服务退出登录");
        return post(url, null, token,
                new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
