// ================================================================
// SELLER SUMMARY DTO
// FreelanceLK.com - Seller Information
// ================================================================
// Summary information for sellers
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for seller summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerSummaryDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private String location;
    private BigDecimal trustScore;
    private Integer totalReviews;
    private BigDecimal averageRating;
}