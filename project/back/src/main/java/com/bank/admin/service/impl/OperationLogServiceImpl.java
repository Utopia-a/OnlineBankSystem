package com.bank.admin.service.impl;

import com.bank.admin.dto.request.OperationLogQueryRequest;
import com.bank.admin.dto.response.OperationLogVO;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.entity.OperationLog;
import com.bank.admin.repository.OperationLogRepository;
import com.bank.admin.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Override
    public PageResult<OperationLogVO> listLogs(OperationLogQueryRequest request) {
        Page<OperationLog> page = operationLogRepository.search(
                request.getKeyword(),
                request.getModule(),
                request.getOperatorId(),
                request.getStartTime(),
                request.getEndTime(),
                request.toSpringPageRequest(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PageResult.of(page.map(OperationLogVO::fromEntity));
    }

    @Override
    @Transactional
    public void record(Long operatorId, String operatorName, String module, String action,
                       String targetType, String targetId, String detail, String ipAddress) {
        OperationLog log = OperationLog.builder()
                .operatorId(operatorId)
                .operatorName(operatorName)
                .module(module)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .detail(detail)
                .ipAddress(ipAddress)
                .build();
        operationLogRepository.save(log);
    }
}
