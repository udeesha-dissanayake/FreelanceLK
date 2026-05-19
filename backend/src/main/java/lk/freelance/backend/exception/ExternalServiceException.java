package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when external service (payment gateway, etc.) fails
 */
public class ExternalServiceException extends FreelanceLKException {
    public ExternalServiceException(String service, String message) {
        super(
                String.format("%s service error: %s", service, message),
                HttpStatus.SERVICE_UNAVAILABLE,
                "EXTERNAL_SERVICE_ERROR"
        );
    }
}