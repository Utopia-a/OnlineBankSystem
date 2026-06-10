package com.banking.report.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 报表模块自定义异常
 */
public class ReportException {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidQueryException extends RuntimeException {
        public InvalidQueryException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ExportTooLargeException extends RuntimeException {
        public ExportTooLargeException(String message) { super(message); }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class TransactionNotFoundException extends RuntimeException {
        public TransactionNotFoundException(String message) { super(message); }
    }
}
