// ================================================================
// REVIEW STATISTICS DTO
// FreelanceLK.com - Review Analytics
// ================================================================
// Contains detailed review statistics
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for review statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatisticsDTO {
    private Integer totalReviews;
    private BigDecimal averageRating;
    private BigDecimal averageCommunicationRating;
    private BigDecimal averageQualityRating;
    private BigDecimal averageProfessionalismRating;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;
    private BigDecimal responseRate; // Percentage of reviews with responses
}