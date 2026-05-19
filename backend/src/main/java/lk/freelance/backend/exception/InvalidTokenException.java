package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an invalid JWT token is provided
 */
public class InvalidTokenException extends FreelanceLKException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
    }
}