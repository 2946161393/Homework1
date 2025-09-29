package com.example.exception.external;

import com.example.exception.common.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception for external service call failures
 */
@Getter
public class ExternalServiceException extends BaseException {
    private final String serviceName;
    private final int statusCode;

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error calling %s: %s", serviceName, message), "EXTERNAL_SERVICE_ERROR");
        this.serviceName = serviceName;
        this.statusCode = 500;
        addInfo("serviceName", serviceName);
        addInfo("statusCode", statusCode);
    }

    public ExternalServiceException(String serviceName, String message, int statusCode) {
        super(String.format("Error calling %s: %s", serviceName, message), "EXTERNAL_SERVICE_ERROR");
        this.serviceName = serviceName;
        this.statusCode = statusCode;
        addInfo("serviceName", serviceName);
        addInfo("statusCode", statusCode);
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error calling %s: %s", serviceName, message), "EXTERNAL_SERVICE_ERROR", cause);
        this.serviceName = serviceName;
        this.statusCode = 500;
        addInfo("serviceName", serviceName);
        addInfo("statusCode", statusCode);
    }

    @Override
    public int getHttpStatus() {
        return statusCode >= 500 ? HttpStatus.BAD_GATEWAY.value() : statusCode;
    }
}