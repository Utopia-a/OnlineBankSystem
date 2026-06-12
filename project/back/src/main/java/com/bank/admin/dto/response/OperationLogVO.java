package com.bank.admin.dto.response;

import com.bank.admin.entity.OperationLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {

    private Long id;
    private Long operatorId;
    private String operatorName;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private String ipAddress;
    private LocalDateTime createdAt;

    public static OperationLogVO fromEntity(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setOperatorId(log.getOperatorId());
        vo.setOperatorName(log.getOperatorName());
        vo.setModule(log.getModule());
        vo.setAction(log.getAction());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId());
        vo.setDetail(log.getDetail());
        vo.setIpAddress(log.getIpAddress());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
