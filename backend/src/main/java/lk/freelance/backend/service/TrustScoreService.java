package lk.freelance.backend.service;

import lk.freelance.backend.dto.TrustScoreDTO;
import lk.freelance.backend.dto.TrustScoreBreakdownDTO; // Must exist
import lk.freelance.backend.dto.TrustScoreThresholdsDTO; // Must exist
import lk.freelance.backend.entity.TrustScore;

import java.util.List; // FIX: Added missing import
import java.util.UUID;

/**
 * Trust Score Calculation and Management Service
 */
public interface TrustScoreService {

    TrustScore calculateTrustScore(UUID userId);

    TrustScoreDTO getTrustScore(UUID userId);

    List<TrustScoreDTO> getTopRatedUsers(String role, int limit);

    void recalculateAllTrustScores();

    TrustScoreBreakdownDTO getTrustScoreBreakdown(UUID userId);

    void updateVerificationScore(UUID userId, Double score);

    void updateResponseTimeScore(UUID userId, Double score);

    TrustScoreThresholdsDTO getTrustScoreThresholds();
}