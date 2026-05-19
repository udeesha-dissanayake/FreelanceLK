// ================================================================
// SELLER DETAIL DTO
// FreelanceLK.com - Complete Seller Information
// ================================================================
// Complete details for sellers
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for seller details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDetailDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private String location;
    private LocalDateTime memberSince;
    private BigDecimal trustScore;
    private Integer totalReviews;
    private BigDecimal averageRating;
    private Integer totalCompletedOrders;
    private BigDecimal completionRate;
    private List<SkillDTO> skills;
}