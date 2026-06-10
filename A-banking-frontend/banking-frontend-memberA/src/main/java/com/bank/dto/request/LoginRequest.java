package com.bank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名或手机号", example = "user001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度应在6-32位之间")
    @Schema(description = "登录密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "OTP 验证码（开启双因素认证时必填）", example = "123456")
    private String otpCode;
}
