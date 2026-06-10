package com.bank.client;

import com.bank.dto.request.DepositWithdrawRequest;
import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.ApiResponse;
import com.bank.dto.response.TransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * 交易服务客户端（对接成员D - 交易服务）
 * 端口：8083
 */
@Component
public class TransactionServiceClient extends BaseServiceClient {

    @Value("${backend.transaction-service.url}")
    private String transactionServiceUrl;

    /**
     * 转账
     */
    public TransactionResponse transfer(TransferRequest request, String token) {
        String url = transactionServiceUrl + "/api/transactions/transfer";
        log.info("发起转账: {} -> {}, 金额: {}",
                request.getFromAccountNo(), request.getToAccountNo(), request.getAmount());
        return post(url, request, token,
                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {});
    }

    /**
     * 存款
     */
    public TransactionResponse deposit(DepositWithdrawRequest request, String token) {
        String url = transactionServiceUrl + "/api/transactions/deposit";
        log.info("存款: 账户 {}, 金额: {}", request.getAccountNo(), request.getAmount());
        return post(url, request, token,
                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {});
    }

    /**
     * 取款
     */
    public TransactionResponse withdraw(DepositWithdrawRequest request, String token) {
        String url = transactionServiceUrl + "/api/transactions/withdraw";
        log.info("取款: 账户 {}, 金额: {}", request.getAccountNo(), request.getAmount());
        return post(url, request, token,
                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {});
    }

    /**
     * 查询单笔交易详情
     */
    public TransactionResponse getTransaction(String transactionId, String token) {
        String url = transactionServiceUrl + "/api/transactions/" + transactionId;
        log.info("查询交易详情: {}", transactionId);
        return get(url, token,
                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {});
    }
}
