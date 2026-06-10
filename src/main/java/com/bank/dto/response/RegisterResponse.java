package com.bank.dto.response;

import lombok.Data;

@Data
public class RegisterResponse {
    private String username;
    private String email;
    private String maskedEmail;
    private String message;
}
