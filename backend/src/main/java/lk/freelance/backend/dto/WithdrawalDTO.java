package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalDTO {
    private UUID withdrawalId;
    private UUID userId; // FIX: Add this
    private BigDecimal amount;
    private String status;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String notes;
}