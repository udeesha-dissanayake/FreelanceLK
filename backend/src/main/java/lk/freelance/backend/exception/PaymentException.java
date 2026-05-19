package lk.freelance.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when payment/transaction operations fail
 */
public class PaymentException extends FreelanceLKException {
    public PaymentException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "PAYMENT_FAILED");
    }
}