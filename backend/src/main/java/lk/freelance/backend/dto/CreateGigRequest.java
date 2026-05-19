// ================================================================
// CREATE GIG REQUEST DTO
// FreelanceLK.com - Gig Creation
// ================================================================
// Request body for creating a new gig
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating gig request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGigRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 255, message = "Title must be between 10 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    private String description;

    @NotNull(message = "Category is required")
    @Size(max = 100)
    private String category;

    @Size(max = 100)
    private String subcategory;

    @NotNull(message = "Pricing model is required")
    private String pricingModel; // "FIXED", "HOURLY", "PACKAGE"

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "100.0", message = "Base price must be at least LKR 100")
    private BigDecimal basePrice;

    @NotNull(message = "Delivery days is required")
    @Min(value = 1, message = "Delivery days must be at least 1")
    @Max(value = 365, message = "Delivery days must not exceed 365")
    private Integer deliveryDays;

    @Min(value = 0, message = "Revisions must be non-negative")
    @Max(value = 10, message = "Revisions must not exceed 10")
    private Integer revisionsIncluded;

    private String requirements;

    private List<UUID> requiredSkillIds;

    private List<String> mediaUrls;

    private List<GigPackageRequest> packages;
}