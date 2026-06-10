package com.bank.web;

import com.bank.account.dto.ApiResponse;
import com.bank.dto.request.LoginRequest;
import com.bank.dto.request.RegisterRequest;
import com.bank.dto.response.LoginResponse;
import com.bank.dto.response.RegisterResponse;
import com.banking.auth.dto.AuthRequest;
import com.banking.auth.dto.AuthResponse;
import com.banking.auth.entity.User;
import com.banking.auth.exception.AuthException;
import com.banking.auth.repository.UserRepository;
import com.banking.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class WebAuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        AuthRequest.Login login = new AuthRequest.Login();
        login.setUsername(request.getUsername());
        login.setPassword(request.getPassword());
        AuthResponse.LoginResult result = authService.login(login, httpRequest.getHeader("User-Agent"));
        return ApiResponse.success("登录成功", toLoginResponse(result));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        AuthRequest.Register reg = new AuthRequest.Register();
        reg.setUsername(request.getUsername());
        reg.setPassword(request.getPassword());
        String email = request.getEmail();
        reg.setEmail(email != null && !email.isBlank()
                ? email.trim() : request.getUsername() + "@bank.local");
        reg.setPhone(request.getPhone());
        reg.setFullName(request.getRealName());
        AuthResponse.RegisterResult result = authService.register(reg);

        RegisterResponse resp = new RegisterResponse();
        resp.setUsername(result.getUsername());
        resp.setEmail(result.getEmail());
        resp.setMaskedEmail(maskEmail(result.getEmail()));
        resp.setMessage(result.getMessage());
        return ApiResponse.success("注册成功，请输入邮箱验证码", resp);
    }

    @GetMapping("/pending-verification")
    public ApiResponse<RegisterResponse> pendingVerification(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException.UserNotFoundException("用户不存在"));
        if (user.getStatus() != User.UserStatus.PENDING_VERIFY) {
            throw new IllegalArgumentException("该账户无需邮箱验证，请直接登录");
        }
        RegisterResponse resp = new RegisterResponse();
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setMaskedEmail(maskEmail(user.getEmail()));
        resp.setMessage("请输入发送到邮箱的 6 位验证码");
        return ApiResponse.success(resp);
    }

    @PostMapping("/verify-email")
    public ApiResponse<String> verifyEmail(@Valid @RequestBody AuthRequest.VerifyOtp request) {
        authService.verifyEmail(request.getTarget(), request.getCode());
        return ApiResponse.success("邮箱验证成功，现在可以登录了", null);
    }

    @PostMapping("/resend-otp")
    public ApiResponse<String> resendOtp(@Valid @RequestBody AuthRequest.ResendOtp request) {
        var result = authService.resendOtp(
                request.getTarget(),
                com.banking.auth.entity.OtpRecord.OtpType.valueOf(request.getType()));
        return ApiResponse.success(result.getMessage(), null);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestParam String refreshToken,
                                              HttpServletRequest httpRequest) {
        AuthResponse.TokenResult result = authService.refreshToken(refreshToken, httpRequest.getHeader("User-Agent"));
        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(result.getAccessToken());
        resp.setRefreshToken(result.getRefreshToken());
        resp.setExpiresIn(result.getExpiresIn());
        resp.setTokenType(result.getTokenType());
        return ApiResponse.success("Token刷新成功", resp);
    }

    @PostMapping("/otp/send")
    public ApiResponse<String> sendOtp(@RequestParam String phone) {
        return ApiResponse.success("验证码已发送", null);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        return ApiResponse.success("已退出登录", null);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) return local.charAt(0) + "***" + domain;
        return local.substring(0, 2) + "***" + domain;
    }

    private LoginResponse toLoginResponse(AuthResponse.LoginResult result) {
        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(result.getAccessToken());
        resp.setRefreshToken(result.getRefreshToken());
        resp.setExpiresIn(result.getExpiresIn());
        resp.setTokenType(result.getTokenType());
        if (result.getUserInfo() != null) {
            resp.setUserId(result.getUserInfo().getId());
            resp.setUsername(result.getUserInfo().getUsername());
            resp.setRealName(result.getUserInfo().getFullName());
            resp.setRole(result.getUserInfo().getRole());
        }
        resp.setRequireOtp(result.isRequireMfa());
        return resp;
    }
}
