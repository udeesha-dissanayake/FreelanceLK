package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when JWT token has expired
 */
public class TokenExpiredException extends FreelanceLKException {
    public TokenExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED");
    }
}