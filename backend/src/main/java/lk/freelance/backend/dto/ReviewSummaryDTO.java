// ================================================================
// REVIEW SUMMARY DTO
// FreelanceLK.com - Review Overview
// ================================================================
// Summary information for reviews
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for review summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDTO {
    private UUID reviewId;
    private UUID reviewerId;
    private String reviewerName;
    private String reviewerAvatar;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private ReviewResponseDTO response;
}