package com.bank.controller;

import com.bank.client.BillServiceClient;
import com.bank.dto.response.PageResponse;
import com.bank.dto.response.TransactionResponse;
import com.bank.interceptor.JwtTokenInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BillController 单元测试
 */
@WebMvcTest(BillController.class)
@DisplayName("BillController 测试")
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillServiceClient billServiceClient;

    @MockBean
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Test
    @DisplayName("查询交易历史 - 返回分页数据")
    void testGetHistory() throws Exception {
        // Mock 拦截器放行
        when(jwtTokenInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        TransactionResponse txn = new TransactionResponse();
        txn.setTransactionId("TXN20240101000001");
        txn.setTransactionType("TRANSFER");
        txn.setAmount(new BigDecimal("500.00"));
        txn.setStatus("SUCCESS");
        txn.setTransactionTime(LocalDateTime.now());

        PageResponse<TransactionResponse> page = PageResponse.of(
                List.of(txn), 1L, 1, 10);

        when(billServiceClient.getTransactionHistory(
                anyString(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/bills/history")
                        .param("accountNo", "6228480000000001")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .header("Authorization", "Bearer mock-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].transactionId")
                        .value("TXN20240101000001"));
    }
}
