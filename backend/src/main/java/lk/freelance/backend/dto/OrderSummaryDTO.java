package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {
    private UUID orderId;
    private String listingTitle;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
    private String sellerName; // FIX: Add this field
    private String thumbnailUrl;
}