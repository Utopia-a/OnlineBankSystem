package com.bank.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
@Schema(description = "登录响应数据")
public class LoginResponse {

    @Schema(description = "JWT Access Token")
    private String accessToken;

    @Schema(description = "JWT Refresh Token")
    private String refreshToken;

    @Schema(description = "Token 类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access Token 过期时间（秒）", example = "3600")
    private Long expiresIn;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "user001")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "用户角色", example = "USER")
    private String role;

    @Schema(description = "是否需要 OTP 验证", example = "false")
    private Boolean requireOtp;
}
