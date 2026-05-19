package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private UUID walletId;

    // FIX: Ensure this field is named 'userId'
    private UUID userId;

    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}