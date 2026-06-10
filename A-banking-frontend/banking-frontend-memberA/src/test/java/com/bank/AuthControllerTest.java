package com.bank.controller;

import com.bank.client.AuthServiceClient;
import com.bank.dto.request.LoginRequest;
import com.bank.dto.response.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试
 */
@WebMvcTest(AuthController.class)
@DisplayName("AuthController 测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthServiceClient authServiceClient;

    @Test
    @DisplayName("登录成功 - 返回 Token")
    void testLoginSuccess() throws Exception {
        LoginResponse mockResp = new LoginResponse();
        mockResp.setAccessToken("mock-jwt-token");
        mockResp.setRefreshToken("mock-refresh-token");
        mockResp.setUsername("user001");
        mockResp.setRealName("张三");
        mockResp.setRole("USER");
        mockResp.setExpiresIn(3600L);
        mockResp.setRequireOtp(false);

        when(authServiceClient.login(any(LoginRequest.class))).thenReturn(mockResp);

        LoginRequest req = new LoginRequest();
        req.setUsername("user001");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("user001"));
    }

    @Test
    @DisplayName("登录失败 - 参数校验（空用户名）")
    void testLoginValidationFail() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("");  // 空用户名，校验应失败
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
