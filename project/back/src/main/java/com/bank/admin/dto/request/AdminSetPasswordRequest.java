package com.bank.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminSetPasswordRequest {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 100, message = "密码至少 8 位")
    private String newPassword;
}
