package com.example.department.exception;

public class ExternalServiceException extends RuntimeException {
    private final String serviceName;
    private final int statusCode;

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error calling %s: %s", serviceName, message));
        this.serviceName = serviceName;
        this.statusCode = 500;
    }

    public ExternalServiceException(String serviceName, String message, int statusCode) {
        super(String.format("Error calling %s: %s", serviceName, message));
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error calling %s: %s", serviceName, message), cause);
        this.serviceName = serviceName;
        this.statusCode = 500;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}