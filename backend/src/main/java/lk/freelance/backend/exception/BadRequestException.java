package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when business logic validation fails
 */
public class BadRequestException extends FreelanceLKException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }
}