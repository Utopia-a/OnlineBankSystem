package com.bank.transaction.service;

import com.bank.transaction.dto.DepositRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.dto.WithdrawRequest;

/**
 * 交易服务接口
 */
public interface TransactionService {

    /**
     * 转账
     * 采用悲观锁保证并发安全，两个账户按 ID 排序加锁防止死锁
     *
     * @param request    转账请求
     * @param operatorId 操作人（来自 JWT）
     * @param operatorIp 操作人 IP
     * @return 交易结果
     */
    TransactionResponse transfer(TransferRequest request, Long operatorId, String operatorIp);

    /**
     * 存款
     *
     * @param request    存款请求
     * @param operatorId 操作人
     * @param operatorIp 操作人 IP
     * @return 交易结果
     */
    TransactionResponse deposit(DepositRequest request, Long operatorId, String operatorIp);

    /**
     * 取款
     *
     * @param request    取款请求
     * @param operatorId 操作人
     * @param operatorIp 操作人 IP
     * @return 交易结果
     */
    TransactionResponse withdraw(WithdrawRequest request, Long operatorId, String operatorIp);
}
