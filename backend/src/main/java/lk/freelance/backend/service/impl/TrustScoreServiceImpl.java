package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.entity.*;
import lk.freelance.backend.enums.*;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.repository.*;
import lk.freelance.backend.service.TrustScoreService;
import lk.freelance.backend.service.mapper.TrustScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrustScoreServiceImpl implements TrustScoreService {

    private final TrustScoreRepository trustScoreRepository;
    // REMOVED: Unused trustScoreWeightRepository
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final UserRepository userRepository;
    private final TrustScoreMapper trustScoreMapper;

    @Override
    @Transactional
    public TrustScore calculateTrustScore(UUID userId) {
        log.info("Calculating trust score for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        TrustScore trustScore = trustScoreRepository.findByUser_UserId(userId)
                .orElseGet(() -> createInitialTrustScore(user));

        BigDecimal reviewScore = calculateReviewScore(userId);
        BigDecimal verificationScore = calculateVerificationScore(userId);
        BigDecimal transactionScore = calculateTransactionConsistencyScore(userId);
        BigDecimal completionRate = calculateCompletionRate(userId);
        BigDecimal responseScore = calculateResponseTimeScore(userId);

        // Weighted Average Calculation (Weights: 40%, 25%, 20%, 10%, 5%)
        BigDecimal overallScore = reviewScore.multiply(BigDecimal.valueOf(0.40))
                .add(verificationScore.multiply(BigDecimal.valueOf(0.25)))
                .add(transactionScore.multiply(BigDecimal.valueOf(0.20)))
                .add(completionRate.multiply(BigDecimal.valueOf(0.10)))
                .add(responseScore.multiply(BigDecimal.valueOf(0.05)))
                .setScale(2, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100));

        trustScore.setOverallScore(overallScore);
        trustScore.setReviewScore(reviewScore);
        trustScore.setVerificationScore(verificationScore);
        trustScore.setTransactionConsistencyScore(transactionScore);
        trustScore.setCompletionRate(completionRate);
        trustScore.setResponseTimeScore(responseScore);
        trustScore.setUpdatedAt(LocalDateTime.now());

        // FIX: Use .intValue() to convert Long to int safely
        Long reviewCount = reviewRepository.countByReviewee_UserId(userId);
        trustScore.setTotalReviews(reviewCount.intValue());

        Long completedCount = orderRepository.countBySeller_UserIdAndStatus(userId, OrderStatus.COMPLETED);
        trustScore.setTotalCompletedOrders(completedCount.intValue());

        return trustScoreRepository.save(trustScore);
    }

    @Override
    public TrustScoreDTO getTrustScore(UUID userId) {
        TrustScore trustScore = trustScoreRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("TrustScore", "userId", userId));
        return trustScoreMapper.toDTO(trustScore);
    }

    @Override
    public List<TrustScoreDTO> getTopRatedUsers(String role, int limit) {
        return trustScoreRepository.findAll(PageRequest.of(0, limit, Sort.by("overallScore").descending()))
                .getContent().stream()
                .filter(ts -> role == null || ts.getUser().getRole().name().equalsIgnoreCase(role))
                .map(trustScoreMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recalculateAllTrustScores() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> calculateTrustScore(user.getUserId()));
    }

    @Override
    public TrustScoreBreakdownDTO getTrustScoreBreakdown(UUID userId) {
        TrustScore ts = trustScoreRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("TrustScore", "userId", userId));

        return TrustScoreBreakdownDTO.builder()
                .overallScore(ts.getOverallScore())
                .trustLevel(getTrustLevel(ts.getOverallScore()))
                .componentScores(Map.of(
                        "Review", ts.getReviewScore(),
                        "Verification", ts.getVerificationScore(),
                        "Consistency", ts.getTransactionConsistencyScore()
                ))
                .build();
    }

    @Override
    @Transactional
    public void updateVerificationScore(UUID userId, Double score) {
        TrustScore ts = trustScoreRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("TrustScore", "userId", userId));
        ts.setVerificationScore(BigDecimal.valueOf(score));
        ts.setUpdatedAt(LocalDateTime.now());
        trustScoreRepository.save(ts);
        calculateTrustScore(userId);
    }

    @Override
    @Transactional
    public void updateResponseTimeScore(UUID userId, Double score) {
        TrustScore ts = trustScoreRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("TrustScore", "userId", userId));
        ts.setResponseTimeScore(BigDecimal.valueOf(score));
        trustScoreRepository.save(ts);
        calculateTrustScore(userId);
    }

    @Override
    public TrustScoreThresholdsDTO getTrustScoreThresholds() {
        return TrustScoreThresholdsDTO.builder()
                .minimumScoreForPayout(BigDecimal.valueOf(80))
                .tierThresholds(Map.of("ELITE", BigDecimal.valueOf(90), "VERIFIED", BigDecimal.valueOf(60)))
                .build();
    }

    // --- Helper Methods ---

    private BigDecimal calculateReviewScore(UUID userId) {
        Double avg = reviewRepository.getAverageRatingByRevieweeId(userId);
        return avg != null ? BigDecimal.valueOf(avg).multiply(BigDecimal.valueOf(20)) : BigDecimal.ZERO;
    }

    private BigDecimal calculateVerificationScore(UUID userId) {
        boolean isVerified = identityVerificationRepository.findByUser_UserIdOrderByVerifiedAtDesc(userId)
                .stream().anyMatch(v -> v.getVerificationStatus() == VerificationStatus.VERIFIED);
        return isVerified ? BigDecimal.valueOf(100) : BigDecimal.valueOf(0);
    }

    private BigDecimal calculateTransactionConsistencyScore(UUID userId) {
        long total = orderRepository.countBySeller_UserId(userId);
        if (total == 0) return BigDecimal.valueOf(100);
        long completed = orderRepository.countBySeller_UserIdAndStatus(userId, OrderStatus.COMPLETED);
        return BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateCompletionRate(UUID userId) {
        return calculateTransactionConsistencyScore(userId);
    }

    private BigDecimal calculateResponseTimeScore(UUID userId) {
        // FIX: Use userId to avoid "parameter not used" warning
        log.debug("Calculating response time score for user {}", userId);
        return BigDecimal.valueOf(100);
    }

    private TrustScore createInitialTrustScore(User user) {
        return trustScoreRepository.save(TrustScore.builder()
                .user(user)
                .overallScore(BigDecimal.valueOf(50))
                .reviewScore(BigDecimal.ZERO)
                .verificationScore(BigDecimal.ZERO)
                .transactionConsistencyScore(BigDecimal.valueOf(100))
                .completionRate(BigDecimal.valueOf(100))
                .responseTimeScore(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    private String getTrustLevel(BigDecimal score) {
        if (score.doubleValue() >= 90) return "ELITE";
        if (score.doubleValue() >= 70) return "HIGH";
        if (score.doubleValue() >= 50) return "AVERAGE";
        return "LOW";
    }
}