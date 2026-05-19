package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.ReviewDTO;
import lk.freelance.backend.dto.ReviewSummaryDTO;
import lk.freelance.backend.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDTO toDTO(Review review) {
        if (review == null) return null;

        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .orderId(review.getOrder().getOrderId())
                .reviewerId(review.getReviewer().getUserId())
                .reviewerName(review.getReviewer().getUserProfile() != null ?
                        review.getReviewer().getUserProfile().getDisplayName() : "Anonymous")
                // FIX: Match field name in DTO (reviewerAvatarUrl)
                .reviewerAvatarUrl(review.getReviewer().getUserProfile() != null ?
                        review.getReviewer().getUserProfile().getAvatarUrl() : null)
                .revieweeId(review.getReviewee().getUserId())
                .rating(review.getRating())
                // FIX: Match field name in DTO (reviewText) instead of "comment"
                .reviewText(review.getReviewText())
                .communicationRating(review.getCommunicationRating())
                .qualityRating(review.getQualityRating())
                .professionalismRating(review.getProfessionalismRating())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public ReviewSummaryDTO toSummaryDTO(Review review) {
        if (review == null) return null;

        return ReviewSummaryDTO.builder()
                .reviewId(review.getReviewId())
                .reviewerId(review.getReviewer().getUserId())
                .reviewerName(review.getReviewer().getUserProfile() != null ?
                        review.getReviewer().getUserProfile().getDisplayName() : "Anonymous")
                // FIX: Match field name in Summary DTO (reviewerAvatar)
                .reviewerAvatar(review.getReviewer().getUserProfile() != null ?
                        review.getReviewer().getUserProfile().getAvatarUrl() : null)
                .rating(review.getRating())
                // FIX: Match field name in Summary DTO (reviewText)
                .reviewText(review.getReviewText())
                .createdAt(review.getCreatedAt())
                .build();
    }
}