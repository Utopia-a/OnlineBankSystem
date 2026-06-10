package com.bank.admin.dto.response;

import com.banking.auth.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String realName;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static UserVO fromEntity(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhoneNumber(user.getPhone());
        vo.setRealName(user.getFullName());
        vo.setStatus(formatStatus(user.getStatus()));
        vo.setRole(formatRole(user.getRole()));
        vo.setCreatedAt(user.getCreatedAt());
        vo.setLastLoginAt(user.getLastLoginAt());
        return vo;
    }

    private static String formatStatus(User.UserStatus status) {
        if (status == User.UserStatus.LOCKED) {
            return "FROZEN";
        }
        return status.name();
    }

    private static String formatRole(User.Role role) {
        return role == User.Role.ROLE_ADMIN ? "ADMIN" : "USER";
    }
}
