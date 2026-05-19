// ================================================================
// CREATE ORDER REQUEST DTO
// FreelanceLK.com - Order Creation
// ================================================================
// Request body for creating a new order
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO for creating order request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    private UUID selectedPackageId; // For gigs with packages

    @Size(max = 1000)
    private String buyerMessage;

    private String paymentMethod; // "CARD", "BANK_TRANSFER", "WALLET"
}