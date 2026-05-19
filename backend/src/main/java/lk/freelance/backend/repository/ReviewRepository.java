package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 1. Find reviews RECEIVED by a user (e.g., Seller profile reviews)
    Page<Review> findByReviewee_UserId(UUID userId, Pageable pageable);

    // 2. FIX: Find reviews WRITTEN by a user (Required for getReviewsByReviewer)
    Page<Review> findByReviewer_UserId(UUID reviewerId, Pageable pageable);

    // 3. Find review for a specific order
    Optional<Review> findByOrder_OrderId(UUID orderId);

    // 4. Calculate average rating
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.userId = :userId")
    Double getAverageRatingByRevieweeId(@Param("userId") UUID userId);

    // 5. Count total reviews received
    long countByReviewee_UserId(UUID userId);
}