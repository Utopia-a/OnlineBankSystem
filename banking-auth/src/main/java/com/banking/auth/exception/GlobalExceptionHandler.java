package com.banking.auth.exception;

import com.banking.auth.dto.AuthResponse.ApiResult;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
    public ResponseEntity<ApiResult<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(ApiResult.fail("参数校验失败: " + errors));
    }

    @ExceptionHandler(AuthException.UserAlreadyExistsException.class)
    public ResponseEntity<ApiResult<Void>> handleUserExists(AuthException.UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.UserNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleUserNotFound(AuthException.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.InvalidCredentialsException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidCredentials(AuthException.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.AccountLockedException.class)
    public ResponseEntity<ApiResult<Void>> handleLocked(AuthException.AccountLockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.AccountNotVerifiedException.class)
    public ResponseEntity<ApiResult<Void>> handleNotVerified(AuthException.AccountNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.InvalidOtpException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidOtp(AuthException.InvalidOtpException ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.InvalidTokenException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidToken(AuthException.InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler(AuthException.TooManyOtpRequestsException.class)
    public ResponseEntity<ApiResult<Void>> handleTooMany(AuthException.TooManyOtpRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResult.fail(ex.getMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ApiResult<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.fail("用户名或密码错误"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResult<Void>> handleSpringLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(ApiResult.fail("账户已锁定，请稍后重试"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResult<Void>> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResult.fail("账户已禁用"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResult.fail("无权限访问"));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResult<Void>> handleJwt(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.fail("Token 无效或已过期"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGeneral(Exception ex) {
        log.error("未处理异常: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail("服务器内部错误，请联系管理员"));
    }
}
