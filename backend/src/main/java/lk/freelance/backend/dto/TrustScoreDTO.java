package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreDTO {
    private BigDecimal overallScore;
    private BigDecimal reviewScore;
    private BigDecimal verificationScore;
    private BigDecimal transactionConsistencyScore;
    private BigDecimal completionRate;
    private BigDecimal responseTimeScore;
    private LocalDateTime updatedAt;
}