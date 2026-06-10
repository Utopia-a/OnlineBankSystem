package com.bank.exception;

/**
 * 后端服务调用异常
 */
public class BackendServiceException extends RuntimeException {

    private final int code;

    public BackendServiceException(String message) {
        super(message);
        this.code = 500;
    }

    public BackendServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public BackendServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
