package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustScoreThresholdsDTO {
    private Map<String, BigDecimal> tierThresholds; // e.g., "VERIFIED": 50, "TOP_RATED": 90
    private BigDecimal minimumScoreForPayout;
}