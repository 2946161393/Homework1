package com.example.exception.common;

import org.springframework.http.HttpStatus;

/**
 * Exception for business rule violations
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR");
    }

    public BusinessException(String message, String errorCode) {
        super(message, errorCode);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, "BUSINESS_ERROR", cause);
    }

    @Override
    public int getHttpStatus() {
        return HttpStatus.CONFLICT.value();
    }
}