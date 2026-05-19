package lk.freelance.backend.exception;

import java.util.UUID;

/**
 * Utility class for common validations and exception throwing
 */
public class ValidationUtils {

    /**
     * Validate that a resource exists, throw exception if not
     */
    public static <T> T requireNonNull(T object, String resourceName, String fieldName, Object fieldValue) {
        if (object == null) {
            throw new ResourceNotFoundException(resourceName, fieldName, fieldValue);
        }
        return object;
    }

    /**
     * Validate that a UUID is valid format
     */
    public static UUID validateUUID(String uuidString, String fieldName) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid UUID format for " + fieldName);
        }
    }

    /**
     * Validate that user has permission to access resource
     */
    public static void requireOwnership(UUID currentUserId, UUID resourceOwnerId, String resourceName) {
        if (!currentUserId.equals(resourceOwnerId)) {
            throw new UnauthorizedException(
                    "You do not have permission to access this " + resourceName
            );
        }
    }

    /**
     * Validate that user is either buyer or seller in an order
     */
    public static void requireOrderParticipant(UUID currentUserId, UUID buyerId, UUID sellerId) {
        if (!currentUserId.equals(buyerId) && !currentUserId.equals(sellerId)) {
            throw new UnauthorizedException(
                    "You do not have permission to access this order"
            );
        }
    }

    /**
     * Validate that user's account is active
     */
    public static void requireActiveAccount(String status) {
        if (!"ACTIVE".equals(status)) {
            if ("SUSPENDED".equals(status)) {
                throw new AccountSuspendedException();
            } else {
                throw new BadRequestException("Account is not active. Status: " + status);
            }
        }
    }

    /**
     * Validate that email is verified
     */
    public static void requireEmailVerified(Boolean emailVerified) {
        if (!emailVerified) {
            throw new EmailNotVerifiedException();
        }
    }

    /**
     * Validate sufficient balance
     */
    public static void requireSufficientBalance(
            java.math.BigDecimal available,
            java.math.BigDecimal required
    ) {
        if (available.compareTo(required) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Required: LKR %.2f, Available: LKR %.2f", required, available)
            );
        }
    }
}