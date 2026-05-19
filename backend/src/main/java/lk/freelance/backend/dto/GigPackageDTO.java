// ================================================================
// GIG PACKAGE DTO
// FreelanceLK.com - Gig Package Details
// ================================================================
// Details of a gig package
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for gig package
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigPackageDTO {
    private UUID packageId;
    private String packageName;
    private String packageDescription;
    private BigDecimal price;
    private Integer deliveryDays;
    private Integer revisions;
}