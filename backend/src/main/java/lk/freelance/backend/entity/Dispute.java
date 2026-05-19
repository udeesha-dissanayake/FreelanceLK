package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // New way for Hibernate 6

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disputes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @GeneratedValue // Hibernate 6 will auto-detect UUID type
    @UuidGenerator  // Simple replacement for GenericGenerator
    private UUID disputeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Ensure Order.java exists in this package

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by", nullable = false)
    private User raisedBy; // Ensure User.java exists in this package

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status; // OPEN, IN_REVIEW, RESOLVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String resolution;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "OPEN";
        }
    }
}