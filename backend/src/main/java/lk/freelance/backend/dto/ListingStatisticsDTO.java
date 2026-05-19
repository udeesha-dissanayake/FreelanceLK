// ================================================================
// LISTING STATISTICS DTO
// FreelanceLK.com - Listing Analytics
// ================================================================
// Contains statistics for seller/employer dashboard
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for listing statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingStatisticsDTO {
    private Integer totalListings;
    private Integer activeListings;
    private Integer draftListings;
    private Integer pausedListings;
    private Integer totalViews;
    private Integer totalOrders;
    private BigDecimal totalRevenue;
}