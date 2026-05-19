package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trust_score_weights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrustScoreWeight {

    @Id
    @GeneratedValue
    @UuidGenerator // Modern Hibernate 6 generator
    @Column(name = "weight_id")
    private UUID weightId;

    @Column(name = "weight_name", nullable = false, unique = true)
    private String weightName; // උදා: "REVIEW_WEIGHT", "VERIFICATION_WEIGHT"

    @Column(name = "weight_value", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightValue; // 0.00 සිට 1.00 දක්වා අගයක්

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}