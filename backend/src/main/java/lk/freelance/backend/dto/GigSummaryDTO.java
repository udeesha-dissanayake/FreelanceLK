// ================================================================
// GIG SUMMARY DTO
// FreelanceLK.com - Gig Overview
// ================================================================
// Summary information for gig listing display
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for gig summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigSummaryDTO {
    private UUID gigId;
    private UUID listingId;
    private String title;
    private String category;
    private String subcategory;
    private BigDecimal basePrice;
    private Integer deliveryDays;
    private String pricingModel;
    private String thumbnailUrl;
    private Integer viewsCount;
    private Boolean isFeatured;
    private LocalDateTime publishedAt;

    // Seller information
    private SellerSummaryDTO seller;

    // Trust metrics
    private BigDecimal averageRating;
    private Integer totalReviews;
}