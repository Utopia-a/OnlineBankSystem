package com.bank.admin.service;

import com.bank.admin.dto.request.TransactionQueryRequest;
import com.bank.admin.dto.response.DashboardStatsVO;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.TransactionVO;

public interface TransactionMonitorService {

    PageResult<TransactionVO> listTransactions(TransactionQueryRequest request);

    TransactionVO getTransactionById(Long transactionId);

    DashboardStatsVO getDashboardStats();

    void cancelTransaction(Long transactionId);
}
