// ================================================================
// ORDER COUNT DTO
// FreelanceLK.com - Order Tracking
// ================================================================
// Contains active order counts by role
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order counts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCountDTO {
    private Integer asBuyer;
    private Integer asSeller;
    private Integer total;
}