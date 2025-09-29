package com.example.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception for validation errors
 */
@Getter
public class ValidationException extends BaseException {
    private final Map<String, String> validationErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = validationErrors != null ? validationErrors : new HashMap<>();
    }

    public ValidationException(String field, String fieldMessage) {
        super("Validation failed", "VALIDATION_ERROR");
        this.validationErrors = new HashMap<>();
        this.validationErrors.put(field, fieldMessage);
    }

    public ValidationException addError(String field, String message) {
        this.validationErrors.put(field, message);
        return this;
    }

    @Override
    public int getHttpStatus() {
        return HttpStatus.BAD_REQUEST.value();
    }
}