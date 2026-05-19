package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when user lacks required permissions
 */
public class UnauthorizedException extends FreelanceLKException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
    }

    public UnauthorizedException() {
        super(
                "You do not have permission to perform this action",
                HttpStatus.FORBIDDEN,
                "UNAUTHORIZED_ACCESS"
        );
    }
}