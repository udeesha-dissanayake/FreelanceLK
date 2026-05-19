package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.TrustScoreDTO;
import lk.freelance.backend.entity.TrustScore;
import org.springframework.stereotype.Component;

@Component
public class TrustScoreMapper {
    public TrustScoreDTO toDTO(TrustScore trustScore) {
        if (trustScore == null) {
            return null;
        }

        return TrustScoreDTO.builder()
                .overallScore(trustScore.getOverallScore())
                .reviewScore(trustScore.getReviewScore()) // Fixed field name
                .verificationScore(trustScore.getVerificationScore())
                .transactionConsistencyScore(trustScore.getTransactionConsistencyScore())
                .completionRate(trustScore.getCompletionRate()) // Fixed field name
                .responseTimeScore(trustScore.getResponseTimeScore()) // Fixed field name
                .updatedAt(trustScore.getUpdatedAt())
                .build();
    }
}