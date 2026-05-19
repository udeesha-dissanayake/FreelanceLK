package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.entity.*;
import lk.freelance.backend.enums.OrderStatus;
import lk.freelance.backend.exception.*;
import lk.freelance.backend.repository.OrderRepository;
import lk.freelance.backend.repository.ReviewRepository;
import lk.freelance.backend.service.ReviewService;
import lk.freelance.backend.service.TrustScoreService;
import lk.freelance.backend.service.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final TrustScoreService trustScoreService;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewDTO createReview(UUID reviewerId, CreateReviewRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (!order.getBuyer().getUserId().equals(reviewerId)) {
            throw new UnauthorizedException("Only the buyer can review this order");
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("You can only review completed orders");
        }

        // Prevent duplicate reviews
        if (reviewRepository.findByOrder_OrderId(order.getOrderId()).isPresent()) {
            throw new BadRequestException("You have already reviewed this order");
        }

        Review review = Review.builder()
                .order(order)
                .reviewer(order.getBuyer())
                .reviewee(order.getSeller())
                .rating(request.getRating())
                .communicationRating(request.getCommunicationRating())
                .qualityRating(request.getQualityRating())
                .professionalismRating(request.getProfessionalismRating())
                .reviewText(request.getReviewText())
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();

        review = reviewRepository.save(review);
        trustScoreService.calculateTrustScore(order.getSeller().getUserId());

        return reviewMapper.toDTO(review);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(UUID userId, UUID reviewId, CreateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getReviewer().getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setCommunicationRating(request.getCommunicationRating());
        review.setQualityRating(request.getQualityRating());
        review.setProfessionalismRating(request.getProfessionalismRating());
        review.setReviewText(request.getReviewText());

        review = reviewRepository.save(review);
        trustScoreService.calculateTrustScore(review.getReviewee().getUserId());

        return reviewMapper.toDTO(review);
    }

    // FIX: Added missing deleteReview method
    @Override
    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Allow deletion if user is the reviewer (Add admin check here later if needed)
        if (!review.getReviewer().getUserId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        UUID sellerId = review.getReviewee().getUserId();
        reviewRepository.delete(review);

        log.info("Review {} deleted by user {}", reviewId, userId);

        // Recalculate trust score for the seller as the review is gone
        trustScoreService.calculateTrustScore(sellerId);
    }

    @Override
    public PagedResponse<ReviewSummaryDTO> getUserReviews(UUID userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByReviewee_UserId(userId, pageable);

        return new PagedResponse<>(
                reviews.getContent().stream().map(reviewMapper::toSummaryDTO).collect(Collectors.toList()),
                page, reviews.getTotalPages(), reviews.getTotalElements(), size, reviews.hasNext(), reviews.hasPrevious()
        );
    }

    @Override
    public PagedResponse<ReviewSummaryDTO> getReviewsByReviewer(UUID reviewerId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByReviewer_UserId(reviewerId, pageable);

        return new PagedResponse<>(
                reviews.getContent().stream().map(reviewMapper::toSummaryDTO).collect(Collectors.toList()),
                page, reviews.getTotalPages(), reviews.getTotalElements(), size, reviews.hasNext(), reviews.hasPrevious()
        );
    }

    @Override
    public ReviewDTO getReviewForOrder(UUID orderId) {
        Review review = reviewRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "orderId", orderId));
        return reviewMapper.toDTO(review);
    }

    @Override
    @Transactional
    public ReviewDTO respondToReview(UUID userId, UUID reviewId, String responseText) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getReviewee().getUserId().equals(userId)) {
            throw new UnauthorizedException("Only the seller can respond");
        }

        ReviewResponse response = ReviewResponse.builder()
                .review(review)
                .responseText(responseText)
                .createdAt(LocalDateTime.now())
                .build();

        review.setResponse(response);
        return reviewMapper.toDTO(reviewRepository.save(review));
    }

    @Override
    public ReviewStatisticsDTO getReviewStatistics(UUID userId) {
        Double avgRating = reviewRepository.getAverageRatingByRevieweeId(userId);
        long count = reviewRepository.countByReviewee_UserId(userId);

        return ReviewStatisticsDTO.builder()
                .totalReviews((int) count)
                .averageRating(java.math.BigDecimal.valueOf(avgRating != null ? avgRating : 0.0))
                .build();
    }

    // FIX: Added missing canReviewOrder method
    @Override
    public boolean canReviewOrder(UUID userId, UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getBuyer().getUserId().equals(userId)
                        && order.getStatus() == OrderStatus.COMPLETED
                        && reviewRepository.findByOrder_OrderId(orderId).isEmpty())
                .orElse(false);
    }
}