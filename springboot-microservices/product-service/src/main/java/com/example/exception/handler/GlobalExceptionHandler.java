package com.example.exception.handler;

import com.example.exception.common.BaseException;
import com.example.exception.common.BusinessException;
import com.example.exception.common.ValidationException;
import com.example.exception.external.ExternalServiceException;
import com.example.exception.resource.DuplicateResourceException;
import com.example.exception.resource.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST controllers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String SERVICE_NAME = "product-service";

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        logger.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://api.example.com/problems/validation-error")
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Validation failed for one or more fields")
                .instance(request.getRequestURI())
                .timestamp(java.time.Instant.now())
                .traceId(MDC.get("traceId"))
                .service(SERVICE_NAME)
                .errorCode("VALIDATION_ERROR")
                .validationErrors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle custom ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleCustomValidation(
            ValidationException ex, HttpServletRequest request) {

        logger.warn("Custom validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        errorResponse.setValidationErrors(ex.getValidationErrors());

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        logger.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        errorResponse.setType("https://api.example.com/problems/not-found");
        errorResponse.setTitle("Resource Not Found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        logger.warn("Duplicate resource: {}", ex.getMessage());

        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        errorResponse.setType("https://api.example.com/problems/duplicate-resource");
        errorResponse.setTitle("Duplicate Resource");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle ExternalServiceException
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceError(
            ExternalServiceException ex, HttpServletRequest request) {

        logger.error("External service error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        errorResponse.setType("https://api.example.com/problems/external-service-error");
        errorResponse.setTitle("External Service Error");

        HttpStatus status = ex.getStatusCode() >= 500 ?
                HttpStatus.BAD_GATEWAY : HttpStatus.valueOf(ex.getStatusCode());

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Handle BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessException ex, HttpServletRequest request) {

        logger.warn("Business rule violation: {}", ex.getMessage());

        ErrorResponse errorResponse = buildErrorResponse(ex, request);
        errorResponse.setType("https://api.example.com/problems/business-rule-violation");
        errorResponse.setTitle("Business Rule Violation");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        logger.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://api.example.com/problems/bad-request")
                .title("Bad Request")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(java.time.Instant.now())
                .traceId(MDC.get("traceId"))
                .service(SERVICE_NAME)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("https://api.example.com/problems/internal-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred. Please try again later.")
                .instance(request.getRequestURI())
                .timestamp(java.time.Instant.now())
                .traceId(MDC.get("traceId"))
                .service(SERVICE_NAME)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Build ErrorResponse from BaseException
     */
    private ErrorResponse buildErrorResponse(BaseException ex, HttpServletRequest request) {
        return ErrorResponse.builder()
                .status(ex.getHttpStatus())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(ex.getTimestamp())
                .traceId(MDC.get("traceId"))
                .service(SERVICE_NAME)
                .errorCode(ex.getErrorCode())
                .additionalInfo(ex.getAdditionalInfo())
                .build();
    }
}