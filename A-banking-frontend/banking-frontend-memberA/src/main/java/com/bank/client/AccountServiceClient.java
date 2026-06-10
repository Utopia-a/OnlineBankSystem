package com.bank.client;

import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 账户服务客户端（对接成员C - 账户管理服务）
 * 端口：8082
 */
@Component
public class AccountServiceClient extends BaseServiceClient {

    @Value("${backend.account-service.url}")
    private String accountServiceUrl;

    /**
     * 查询当前用户的所有账户
     */
    public List<AccountResponse> getMyAccounts(String token) {
        String url = accountServiceUrl + "/api/accounts/my";
        log.info("查询用户账户列表");
        return get(url, token, new ParameterizedTypeReference<ApiResponse<List<AccountResponse>>>() {});
    }

    /**
     * 根据账户号查询账户详情
     */
    public AccountResponse getAccountByNo(String accountNo, String token) {
        String url = accountServiceUrl + "/api/accounts/" + accountNo;
        log.info("查询账户详情: {}", accountNo);
        return get(url, token, new ParameterizedTypeReference<ApiResponse<AccountResponse>>() {});
    }

    /**
     * 查询账户余额
     */
    public AccountResponse getBalance(String accountNo, String token) {
        String url = accountServiceUrl + "/api/accounts/" + accountNo + "/balance";
        log.info("查询账户余额: {}", accountNo);
        return get(url, token, new ParameterizedTypeReference<ApiResponse<AccountResponse>>() {});
    }

    /**
     * 开户
     */
    public AccountResponse createAccount(String accountType, String token) {
        String url = accountServiceUrl + "/api/accounts?type=" + accountType;
        log.info("开户请求，账户类型: {}", accountType);
        return post(url, null, token, new ParameterizedTypeReference<ApiResponse<AccountResponse>>() {});
    }

    /**
     * 冻结账户（管理员操作）
     */
    public String freezeAccount(String accountNo, String token) {
        String url = accountServiceUrl + "/api/accounts/" + accountNo + "/freeze";
        log.info("冻结账户: {}", accountNo);
        return put(url, null, token, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }

    /**
     * 解冻账户（管理员操作）
     */
    public String unfreezeAccount(String accountNo, String token) {
        String url = accountServiceUrl + "/api/accounts/" + accountNo + "/unfreeze";
        log.info("解冻账户: {}", accountNo);
        return put(url, null, token, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
}
