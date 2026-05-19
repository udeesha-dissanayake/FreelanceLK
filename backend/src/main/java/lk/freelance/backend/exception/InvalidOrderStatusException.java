package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when order status transition is invalid
 */
public class InvalidOrderStatusException extends FreelanceLKException {
    public InvalidOrderStatusException(String currentStatus, String newStatus) {
        super(
                String.format("Cannot transition order from %s to %s", currentStatus, newStatus),
                HttpStatus.BAD_REQUEST,
                "INVALID_ORDER_STATUS_TRANSITION"
        );
    }
}