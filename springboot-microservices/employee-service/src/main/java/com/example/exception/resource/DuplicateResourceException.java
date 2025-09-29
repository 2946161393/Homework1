package com.example.exception.resource;

import com.example.exception.common.BusinessException;

/**
 * Exception thrown when attempting to create a resource that already exists
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s already exists with identifier: %s", resourceType, identifier),
                "DUPLICATE_RESOURCE");
        addInfo("resourceType", resourceType);
        addInfo("identifier", identifier);
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}