// ================================================================
// REVIEW CONTROLLER
// FreelanceLK.com - Review & Rating System
// ================================================================
// Handles review creation, responses, and trust score updates
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-01-30
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.service.ReviewService;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for review and rating operations
 * Base URL: /api/v1/reviews
 *
 * @apiNote Manages review lifecycle and trust score calculations
 * @security Reviewers can only review their own completed orders
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Create a review for a completed order
     * POST /api/v1/reviews
     *
     * @param currentUser Authenticated user (reviewer)
     * @param request Review details
     * @return Created review
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewDTO review = reviewService.createReview(
                currentUser.getUserId(),
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Review submitted successfully! The seller's trust score will be updated.",
                        review
                ));
    }

    /**
     * Get review for a specific order
     * GET /api/v1/reviews/order/{orderId}
     *
     * @param orderId Order ID
     * @return Review for the order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<ReviewDTO>> getReviewForOrder(
            @PathVariable UUID orderId
    ) {
        ReviewDTO review = reviewService.getReviewForOrder(orderId);

        return ResponseEntity
                .ok(ApiResponse.success(review));
    }

    /**
     * Get all reviews for a user (as reviewee)
     * GET /api/v1/reviews/user/{userId}
     *
     * @param userId User ID (reviewee)
     * @param page Page number
     * @param size Page size
     * @return Paginated reviews
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewSummaryDTO>>> getUserReviews(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<ReviewSummaryDTO> reviews = reviewService.getUserReviews(
                userId,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(reviews));
    }

    /**
     * Get reviews received by current user
     * GET /api/v1/reviews/received
     *
     * @param currentUser Authenticated user
     * @param page Page number
     * @param size Page size
     * @return Reviews received
     */
    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewSummaryDTO>>> getReceivedReviews(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<ReviewSummaryDTO> reviews = reviewService.getUserReviews(
                currentUser.getUserId(),
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(reviews));
    }

    /**
     * Get reviews given by current user
     * GET /api/v1/reviews/given
     *
     * @param currentUser Authenticated user
     * @param page Page number
     * @param size Page size
     * @return Reviews given
     */
    @GetMapping("/given")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewSummaryDTO>>> getGivenReviews(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<ReviewSummaryDTO> reviews = reviewService.getReviewsByReviewer(
                currentUser.getUserId(),
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(reviews));
    }

    /**
     * Respond to a review
     * POST /api/v1/reviews/{reviewId}/respond
     *
     * @param currentUser Authenticated user (reviewee)
     * @param reviewId Review ID
     * @param request Response details
     * @return Updated review with response
     */
    @PostMapping("/{reviewId}/respond")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewDTO>> respondToReview(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID reviewId,
            @Valid @RequestBody RespondToReviewRequest request
    ) {
        ReviewDTO review = reviewService.respondToReview(
                currentUser.getUserId(),
                reviewId,
                request.getResponseText()
        );

        return ResponseEntity
                .ok(ApiResponse.success("Response added successfully!", review));
    }

    /**
     * Update review (within 24 hours of creation)
     * PUT /api/v1/reviews/{reviewId}
     *
     * @param currentUser Authenticated user (reviewer)
     * @param reviewId Review ID
     * @param request Updated review details
     * @return Updated review
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID reviewId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewDTO review = reviewService.updateReview(
                currentUser.getUserId(),
                reviewId,
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success("Review updated successfully!", review));
    }

    /**
     * Delete review (within 24 hours of creation)
     * DELETE /api/v1/reviews/{reviewId}
     *
     * @param currentUser Authenticated user (reviewer)
     * @param reviewId Review ID
     * @return Success message
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID reviewId
    ) {
        reviewService.deleteReview(currentUser.getUserId(), reviewId);

        return ResponseEntity
                .ok(ApiResponse.success("Review deleted successfully!"));
    }

    /**
     * Get review statistics for a user
     * GET /api/v1/reviews/user/{userId}/stats
     *
     * @param userId User ID
     * @return Review statistics
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<ApiResponse<ReviewStatisticsDTO>> getReviewStatistics(
            @PathVariable UUID userId
    ) {
        ReviewStatisticsDTO stats = reviewService.getReviewStatistics(userId);

        return ResponseEntity
                .ok(ApiResponse.success(stats));
    }

    /**
     * Check if user can review an order
     * GET /api/v1/reviews/can-review/{orderId}
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @return Boolean indicating if review is possible
     */
    @GetMapping("/can-review/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> canReviewOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        boolean canReview = reviewService.canReviewOrder(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success(canReview));
    }
}