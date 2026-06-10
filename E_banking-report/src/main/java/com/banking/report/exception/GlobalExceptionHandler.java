package com.banking.report.exception;

import com.banking.report.dto.ReportResponse.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
                errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiResult.fail("参数校验失败: " + errors));
    }

    @ExceptionHandler(ReportException.AccountNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleAccountNotFound(
            ReportException.AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(ReportException.AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDenied(
            ReportException.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(ReportException.InvalidQueryException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidQuery(
            ReportException.InvalidQueryException ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(ReportException.ExportTooLargeException.class)
    public ResponseEntity<ApiResult<Void>> handleExportTooLarge(
            ReportException.ExportTooLargeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(ReportException.TransactionNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleTxNotFound(
            ReportException.TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<Void>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGeneral(Exception ex) {
        log.error("未处理异常: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail("服务器内部错误，请联系管理员"));
    }
}
