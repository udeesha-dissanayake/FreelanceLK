package lk.freelance.backend.exception;

/**
 * Centralized error code constants for consistent error handling
 */
public final class ErrorCodes {

    // Authentication & Authorization
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
    public static final String ACCOUNT_SUSPENDED = "ACCOUNT_SUSPENDED";

    // Resource Operations
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String RESOURCE_ALREADY_EXISTS = "RESOURCE_ALREADY_EXISTS";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String GIG_NOT_FOUND = "GIG_NOT_FOUND";
    public static final String JOB_NOT_FOUND = "JOB_NOT_FOUND";
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String REVIEW_NOT_FOUND = "REVIEW_NOT_FOUND";

    // Validation
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";

    // Payment & Transactions
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
    public static final String WITHDRAWAL_FAILED = "WITHDRAWAL_FAILED";

    // Order Operations
    public static final String INVALID_ORDER_STATUS_TRANSITION = "INVALID_ORDER_STATUS_TRANSITION";
    public static final String ORDER_ALREADY_REVIEWED = "ORDER_ALREADY_REVIEWED";
    public static final String ORDER_NOT_COMPLETED = "ORDER_NOT_COMPLETED";

    // File Operations
    public static final String FILE_UPLOAD_FAILED = "FILE_UPLOAD_FAILED";
    public static final String INVALID_FILE_TYPE = "INVALID_FILE_TYPE";
    public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";

    // External Services
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
    public static final String EMAIL_SERVICE_ERROR = "EMAIL_SERVICE_ERROR";
    public static final String PAYMENT_GATEWAY_ERROR = "PAYMENT_GATEWAY_ERROR";

    // System
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String NULL_POINTER = "NULL_POINTER";

    private ErrorCodes() {
        // Private constructor to prevent instantiation
    }
}