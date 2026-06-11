package com.bank.admin.dto.request;

import com.banking.auth.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotNull(message = "状态不能为空")
    private User.UserStatus status;
}
