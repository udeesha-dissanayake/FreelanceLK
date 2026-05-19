package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private UUID reviewId;
    private UUID orderId;
    private UUID reviewerId;
    private String reviewerName;
    private String reviewerAvatarUrl; // ADD THIS FIELD
    private UUID revieweeId;
    private String revieweeName;
    private Integer rating;
    private String reviewText; // Use 'reviewText' to match entity
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Detailed ratings
    private Integer communicationRating;
    private Integer qualityRating;
    private Integer professionalismRating;
}