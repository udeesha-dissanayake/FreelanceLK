// ================================================================
// RESPOND TO REVIEW REQUEST DTO
// FreelanceLK.com - Review Response
// ================================================================
// Request body for responding to a review
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO for respond to review request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespondToReviewRequest {

    @NotNull(message = "Review ID is required")
    private UUID reviewId;

    @NotBlank(message = "Response text is required")
    @Size(min = 10, max = 500, message = "Response must be between 10 and 500 characters")
    private String responseText;
}