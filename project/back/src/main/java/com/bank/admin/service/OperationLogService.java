package com.bank.admin.service;

import com.bank.admin.dto.request.OperationLogQueryRequest;
import com.bank.admin.dto.response.OperationLogVO;
import com.bank.admin.dto.response.PageResult;

public interface OperationLogService {

    PageResult<OperationLogVO> listLogs(OperationLogQueryRequest request);

    void record(Long operatorId, String operatorName, String module, String action,
                String targetType, String targetId, String detail, String ipAddress);
}
