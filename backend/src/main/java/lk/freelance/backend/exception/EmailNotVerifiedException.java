package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when email verification is required but not completed
 */
public class EmailNotVerifiedException extends FreelanceLKException {
    public EmailNotVerifiedException() {
        super(
                "Please verify your email address to continue",
                HttpStatus.FORBIDDEN,
                "EMAIL_NOT_VERIFIED"
        );
    }
}