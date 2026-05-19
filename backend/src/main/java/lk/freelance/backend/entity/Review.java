package lk.freelance.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID reviewId;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @ManyToOne
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    private Integer rating;
    private Integer communicationRating;
    private Integer qualityRating;
    private Integer professionalismRating;

    @Column(columnDefinition = "TEXT")
    private String reviewText;

    private boolean isPublic;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL)
    private ReviewResponse response;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}