package lk.freelance.backend.entity; // Added missing package

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator; // Hibernate 6 replacement
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trust_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class TrustScore {

    @Id
    @GeneratedValue
    @UuidGenerator // Modern Hibernate 6 generator
    @Column(name = "trust_score_id")
    private UUID trustScoreId;

    @OneToOne(fetch = FetchType.LAZY) // Optimized for performance
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "overall_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "review_score", precision = 5, scale = 2)
    private BigDecimal reviewScore;

    @Column(name = "verification_score", precision = 5, scale = 2)
    private BigDecimal verificationScore;

    @Column(name = "transaction_consistency_score", precision = 5, scale = 2)
    private BigDecimal transactionConsistencyScore;

    @Column(name = "completion_rate", precision = 5, scale = 2)
    private BigDecimal completionRate;

    @Column(name = "response_time_score", precision = 5, scale = 2)
    private BigDecimal responseTimeScore;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "total_completed_orders")
    private Integer totalCompletedOrders;

    @Column(name = "total_cancelled_orders")
    private Integer totalCancelledOrders;

    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.lastCalculatedAt = now;

        // Initialize BigDecimals to Zero if null
        if (overallScore == null) overallScore = BigDecimal.ZERO;
        if (reviewScore == null) reviewScore = BigDecimal.ZERO;
        if (verificationScore == null) verificationScore = BigDecimal.ZERO;
        if (transactionConsistencyScore == null) transactionConsistencyScore = BigDecimal.ZERO;
        if (completionRate == null) completionRate = BigDecimal.ZERO;
        if (responseTimeScore == null) responseTimeScore = BigDecimal.ZERO;

        // Initialize Integers to Zero if null
        if (totalReviews == null) totalReviews = 0;
        if (totalCompletedOrders == null) totalCompletedOrders = 0;
        if (totalCancelledOrders == null) totalCancelledOrders = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}