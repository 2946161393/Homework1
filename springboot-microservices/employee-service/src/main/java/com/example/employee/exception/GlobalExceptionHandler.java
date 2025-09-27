package com.example.employee.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));

        // Add custom properties
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("service", "employee-service");

        // Collect validation errors
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("message", error.getDefaultMessage());
                    errorMap.put("rejectedValue", String.valueOf(error.getRejectedValue()));
                    return errorMap;
                })
                .toList();

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("service", "employee-service");
        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("resourceId", ex.getResourceId());

        return problemDetail;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        logger.warn("Duplicate resource: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/duplicate-resource"));
        problemDetail.setTitle("Duplicate Resource");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("service", "employee-service");
        problemDetail.setProperty("errorCode", ex.getErrorCode());

        return problemDetail;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleCustomValidation(ValidationException ex, HttpServletRequest request) {
        logger.warn("Custom validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("service", "employee-service");
        problemDetail.setProperty("validationErrors", ex.getValidationErrors());

        return problemDetail;
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalServiceError(ExternalServiceException ex, HttpServletRequest request) {
        logger.error("External service error: {}", ex.getMessage(), ex);

        HttpStatus status = ex.getStatusCode() >= 500 ?
                HttpStatus.BAD_GATEWAY : HttpStatus.valueOf(ex.getStatusCode());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                "External service unavailable"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/external-service-error"));
        problemDetail.setTitle("External Service Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("service", "employee-service");
        problemDetail.setProperty("externalService", ex.getServiceName());
        problemDetail.setProperty("externalStatusCode", ex.getStatusCode());

        return problemDetail;
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessRule(BusinessException ex, HttpServletRequest request) {
        logger.warn("Business rule violation: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/business-rule-violation"));
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("service", "employee-service");
        problemDetail.setProperty("errorCode", ex.getErrorCode());

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));
        problemDetail.setProperty("service", "employee-service");

        return problemDetail;
    }
}