// ================================================================
// CREATE REVIEW REQUEST DTO
// FreelanceLK.com - Review Creation
// ================================================================
// Request body for creating a review
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO for create review request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Min(value = 1)
    @Max(value = 5)
    private Integer communicationRating;

    @Min(value = 1)
    @Max(value = 5)
    private Integer qualityRating;

    @Min(value = 1)
    @Max(value = 5)
    private Integer professionalismRating;

    @Size(min = 10, max = 1000, message = "Review text must be between 10 and 1000 characters")
    private String reviewText;
}