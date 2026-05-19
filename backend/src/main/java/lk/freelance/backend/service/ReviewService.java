package lk.freelance.backend.service;

import lk.freelance.backend.dto.*;
import java.util.UUID;

public interface ReviewService {

    ReviewDTO createReview(UUID userId, CreateReviewRequest request);

    ReviewDTO getReviewForOrder(UUID orderId);

    // Fixed: Standardized argument names and counts for pagination
    PagedResponse<ReviewSummaryDTO> getUserReviews(UUID userId, Integer page, Integer size);

    // Fixed: Added missing method for reviews given by the user
    PagedResponse<ReviewSummaryDTO> getReviewsByReviewer(UUID userId, Integer page, Integer size);

    // Fixed: Argument type matches Controller (String responseText)
    ReviewDTO respondToReview(UUID userId, UUID reviewId, String responseText);

    // Fixed: Using CreateReviewRequest as per your Controller logic
    ReviewDTO updateReview(UUID userId, UUID reviewId, CreateReviewRequest request);

    void deleteReview(UUID userId, UUID reviewId);

    // Fixed: Added missing statistics method
    ReviewStatisticsDTO getReviewStatistics(UUID userId);

    // Fixed: Added missing validation check
    boolean canReviewOrder(UUID userId, UUID orderId);
}