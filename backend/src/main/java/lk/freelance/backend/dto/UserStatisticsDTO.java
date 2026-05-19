package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    private UUID userId;
    private Integer activeOrders;      // IN_PROGRESS orders
    private BigDecimal pendingEarnings; // money in escrow (not yet released)
    private BigDecimal totalEarned;    // all-time released earnings
    private Integer completedOrders;   // total COMPLETED orders
    private BigDecimal trustScore;
}
