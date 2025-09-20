package com.example.department.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
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

        // Collect validation errors
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("message", error.getDefaultMessage());
                    return errorMap;
                })
                .toList();

        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBusinessRuleViolation(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/business-rule-violation"));
        problemDetail.setTitle("Business Rule Violation");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("traceId", MDC.get("traceId"));

        return problemDetail;
    }
}