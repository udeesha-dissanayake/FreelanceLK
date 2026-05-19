// ================================================================
// ORDER STATISTICS DTO
// FreelanceLK.com - Order Analytics
// ================================================================
// Contains order statistics for dashboard
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for order statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatisticsDTO {
    private Integer totalOrders;
    private Integer activeOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
    private BigDecimal totalSpent; // As buyer
    private BigDecimal totalEarned; // As seller
    private BigDecimal averageOrderValue;
    private BigDecimal completionRate;
}