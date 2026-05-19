// ================================================================
// GIG DETAIL DTO
// FreelanceLK.com - Complete Gig Information
// ================================================================
// Complete details of a gig listing
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
 * DTO for gig details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigDetailDTO {
    private UUID gigId;
    private UUID listingId;
    private String title;
    private String description;
    private String category;
    private String subcategory;
    private String pricingModel;
    private BigDecimal basePrice;
    private Integer deliveryDays;
    private Integer revisionsIncluded;
    private String requirements;
    private String status;
    private Integer viewsCount;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    // Media
    private List<String> mediaUrls;

    // Packages
    private List<GigPackageDTO> packages;

    // Skills
    private List<SkillDTO> requiredSkills;

    // Seller
    private SellerDetailDTO seller;

    // Reviews
    private List<ReviewSummaryDTO> reviews;
    private BigDecimal averageRating;
    private Integer totalReviews;
}