package com.example.department.exception;

import java.util.Map;
import java.util.HashMap;

public class ValidationException extends RuntimeException {
    private final Map<String, String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors != null ? validationErrors : new HashMap<>();
    }

    public ValidationException(String field, String fieldMessage) {
        super("Validation failed");
        this.validationErrors = new HashMap<>();
        this.validationErrors.put(field, fieldMessage);
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public ValidationException addError(String field, String message) {
        this.validationErrors.put(field, message);
        return this;
    }
}