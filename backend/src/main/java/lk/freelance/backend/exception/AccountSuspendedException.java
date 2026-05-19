package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when user account is suspended or inactive
 */
public class AccountSuspendedException extends FreelanceLKException {
    public AccountSuspendedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED");
    }

    public AccountSuspendedException() {
        super(
                "Your account has been suspended. Please contact support.",
                HttpStatus.FORBIDDEN,
                "ACCOUNT_SUSPENDED"
        );
    }
}