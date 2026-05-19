package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails
 */
public class AuthenticationException extends FreelanceLKException {
    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED");
    }
}