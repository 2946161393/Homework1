package com.example.exception.common;

import lombok.Getter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all custom exceptions
 */
@Getter
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final Instant timestamp;
    private final Map<String, Object> additionalInfo;

    protected BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.additionalInfo = new HashMap<>();
    }

    protected BaseException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
        this.additionalInfo = new HashMap<>();
    }

    public BaseException addInfo(String key, Object value) {
        this.additionalInfo.put(key, value);
        return this;
    }

    public abstract int getHttpStatus();
}