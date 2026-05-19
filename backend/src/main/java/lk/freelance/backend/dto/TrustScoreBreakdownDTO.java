package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreBreakdownDTO {
    private BigDecimal overallScore;
    private Map<String, BigDecimal> componentScores; // e.g., "Verification": 90
    private String trustLevel; // e.g., "High Trust", "Elite"
    private String nextLevelRequirement;
}