package com.bank.admin.dto.request;

import com.banking.auth.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest {

    private String keyword;
    private User.UserStatus status;
    private User.Role role;
}
