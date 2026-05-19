// ================================================================
// SEARCH LISTINGS REQUEST DTO
// FreelanceLK.com - Search Filters
// ================================================================
// Request body for searching listings with filters
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for search listings request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchListingsRequest {
    private String query;
    private String listingType; // "GIG" or "PART_TIME_JOB"
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer maxDeliveryDays;
    private Boolean isRemote; // For jobs
    private String sortBy; // "PRICE_ASC", "PRICE_DESC", "NEWEST", "TRUST_SCORE"
    private Integer page = 0;
    private Integer size = 20;
}