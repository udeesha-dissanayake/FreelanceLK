package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private UUID orderId;
    private UUID listingId;
    private String listingTitle;
    private UUID buyerId;
    private String buyerName;
    private UUID sellerId;
    private String sellerName;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}