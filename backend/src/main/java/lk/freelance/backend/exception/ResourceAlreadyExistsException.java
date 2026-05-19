package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when attempting to create a resource that already exists
 */
public class ResourceAlreadyExistsException extends FreelanceLKException {
    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT,
                "RESOURCE_ALREADY_EXISTS"
        );
    }

    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS");
    }
}