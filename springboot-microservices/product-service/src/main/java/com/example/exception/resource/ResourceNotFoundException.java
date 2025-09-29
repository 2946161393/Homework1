package com.example.exception.resource;

import com.example.exception.common.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found
 */
@Getter
public class ResourceNotFoundException extends BaseException {
    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId), "RESOURCE_NOT_FOUND");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        addInfo("resourceType", resourceType);
        addInfo("resourceId", resourceId);
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
        this.resourceType = "Resource";
        this.resourceId = "unknown";
    }

    @Override
    public int getHttpStatus() {
        return HttpStatus.NOT_FOUND.value();
    }
}