package lk.freelance.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom exceptions
 */
@Getter
public class FreelanceLKException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public FreelanceLKException(String message, HttpStatus status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public FreelanceLKException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = status.getReasonPhrase();
    }
}