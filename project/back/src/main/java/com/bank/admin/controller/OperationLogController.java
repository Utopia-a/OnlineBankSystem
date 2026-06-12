package com.bank.admin.controller;

import com.bank.admin.dto.request.OperationLogQueryRequest;
import com.bank.admin.dto.response.OperationLogVO;
import com.bank.admin.dto.response.PageResult;
import com.bank.admin.dto.response.Result;
import com.bank.admin.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员-操作日志", description = "查看管理员操作审计记录")
@RestController
@RequestMapping("/api/admin/operation-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public Result<PageResult<OperationLogVO>> list(OperationLogQueryRequest request) {
        return Result.success(operationLogService.listLogs(request));
    }
}
