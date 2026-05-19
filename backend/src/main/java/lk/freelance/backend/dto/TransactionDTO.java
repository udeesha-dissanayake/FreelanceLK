package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID transactionId;
    private UUID orderId;
    private UUID userId; // Use userId instead of walletId for clarity
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}