package com.bank.controller;

import com.bank.client.TransactionServiceClient;
import com.bank.dto.request.DepositWithdrawRequest;
import com.bank.dto.request.TransferRequest;
import com.bank.dto.response.ApiResponse;
import com.bank.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 交易 API 代理控制器
 * 转发至成员D的交易服务（端口8083）
 */
@Tag(name = "交易服务", description = "转账、存款、取款、交易查询接口（对接成员D）")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionServiceClient transactionServiceClient;

    @Operation(
            summary = "转账",
            description = """
                    发起账户间转账。单笔限额100万元。
                    若转账金额超过5000元，需要在请求中附带 OTP 验证码。
                    转账操作在事务中完成，失败时自动回滚。
                    """
    )
    @PostMapping("/transfer")
    public ApiResponse<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {
        String token = (String) httpRequest.getAttribute("jwtToken");
        TransactionResponse result = transactionServiceClient.transfer(request, token);
        return ApiResponse.success("转账成功", result);
    }

    @Operation(summary = "存款", description = "向指定账户存入资金，单次最大50万元")
    @PostMapping("/deposit")
    public ApiResponse<TransactionResponse> deposit(
            @Valid @RequestBody DepositWithdrawRequest request,
            HttpServletRequest httpRequest) {
        String token = (String) httpRequest.getAttribute("jwtToken");
        TransactionResponse result = transactionServiceClient.deposit(request, token);
        return ApiResponse.success("存款成功", result);
    }

    @Operation(summary = "取款", description = "从指定账户取出资金，需账户余额充足，单次最大50万元")
    @PostMapping("/withdraw")
    public ApiResponse<TransactionResponse> withdraw(
            @Valid @RequestBody DepositWithdrawRequest request,
            HttpServletRequest httpRequest) {
        String token = (String) httpRequest.getAttribute("jwtToken");
        TransactionResponse result = transactionServiceClient.withdraw(request, token);
        return ApiResponse.success("取款成功", result);
    }

    @Operation(summary = "查询交易详情", description = "根据交易ID查询单笔交易的详细信息")
    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionResponse> getTransaction(
            @Parameter(description = "交易ID", required = true, example = "TXN20240101000001")
            @PathVariable String transactionId,
            HttpServletRequest httpRequest) {
        String token = (String) httpRequest.getAttribute("jwtToken");
        TransactionResponse result = transactionServiceClient.getTransaction(transactionId, token);
        return ApiResponse.success(result);
    }
}
