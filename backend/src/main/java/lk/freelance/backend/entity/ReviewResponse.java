package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 generator
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "review_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    @Id
    @GeneratedValue // Auto-detects UUID type
    @UuidGenerator  // Clean replacement for GenericGenerator
    @Column(name = "response_id")
    private UUID responseId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "response_text", columnDefinition = "TEXT", nullable = false)
    private String responseText;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}