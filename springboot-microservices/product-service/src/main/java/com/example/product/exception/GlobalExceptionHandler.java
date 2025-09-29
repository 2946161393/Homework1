package com.example.product.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ProblemDetail handleClientError(HttpClientErrorException ex, HttpServletRequest request) {
        log.error("Client error calling external API: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                ex.getStatusCode(),
                "Error calling FakeStore API"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/external-api-client-error"));
        problemDetail.setTitle("External API Client Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("service", "product-service");

        return problemDetail;
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ProblemDetail handleServerError(HttpServerErrorException ex, HttpServletRequest request) {
        log.error("Server error calling external API: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "FakeStore API is temporarily unavailable"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/external-api-server-error"));
        problemDetail.setTitle("External API Server Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("service", "product-service");

        return problemDetail;
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ProblemDetail handleResourceAccessException(ResourceAccessException ex, HttpServletRequest request) {
        log.error("Network error accessing external API: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to connect to FakeStore API"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/network-error"));
        problemDetail.setTitle("Network Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("service", "product-service");

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error in product service: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );

        problemDetail.setType(java.net.URI.create("https://api.example.com/problems/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("service", "product-service");

        return problemDetail;
    }
}