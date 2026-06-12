package com.banking.config;

import com.bank.account.dto.ApiResponse;
import com.bank.account.exception.AccountAccessDeniedException;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.AccountStatusException;
import com.bank.account.exception.InsufficientBalanceException;
import com.bank.transaction.exception.BusinessException;
import com.bank.transaction.exception.TransactionLimitException;
import com.banking.auth.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("账户不存在: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(4001, ex.getMessage()));
    }

    @ExceptionHandler({
            InsufficientBalanceException.class,
            AccountStatusException.class,
            AccountAccessDeniedException.class,
            TransactionLimitException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleAccountBusiness(RuntimeException ex) {
        log.warn("账户/交易业务异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        String message = errors.values().stream().findFirst().orElse("请求参数不合法");
        ApiResponse<Map<String, String>> resp = ApiResponse.error(400, message);
        resp.setData(errors);
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "权限不足，需要管理员权限"));
    }

    @ExceptionHandler({
            AuthException.InvalidOtpException.class,
            AuthException.TooManyOtpRequestsException.class,
            AuthException.UserNotFoundException.class,
            AuthException.AccountLockedException.class,
            AuthException.AccountNotVerifiedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleAuthBusiness(RuntimeException ex) {
        log.warn("认证业务异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex) {
        log.error("运行时异常: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, ex.getMessage()));
    }
}
