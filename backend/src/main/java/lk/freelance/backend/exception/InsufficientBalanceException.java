package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when insufficient wallet balance
 */
public class InsufficientBalanceException extends FreelanceLKException {
    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_BALANCE");
    }

    public InsufficientBalanceException() {
        super(
                "Insufficient wallet balance for this transaction",
                HttpStatus.PAYMENT_REQUIRED,
                "INSUFFICIENT_BALANCE"
        );
    }
}