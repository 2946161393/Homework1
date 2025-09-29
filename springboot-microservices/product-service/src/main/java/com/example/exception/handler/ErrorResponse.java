package com.example.exception.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response structure
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private Instant timestamp;
    private String traceId;
    private String service;
    private String errorCode;
    private Map<String, Object> additionalInfo;
    private Map<String, String> validationErrors;

    public static ErrorResponse of(String type, String title, int status, String detail, String instance) {
        return ErrorResponse.builder()
                .type(type)
                .title(title)
                .status(status)
                .detail(detail)
                .instance(instance)
                .timestamp(Instant.now())
                .build();
    }
}