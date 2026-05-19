package lk.freelance.backend.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private UUID orderId;
    private UUID listingId;
    private String listingTitle;
    private UUID buyerId; // <--- ADD THIS FIELD
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
    private List<DeliverableDTO> deliverables;
}