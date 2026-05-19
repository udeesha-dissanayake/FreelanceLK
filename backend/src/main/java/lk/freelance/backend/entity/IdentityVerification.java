package lk.freelance.backend.entity;

import jakarta.persistence.*;
import lk.freelance.backend.enums.VerificationStatus;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "identity_verifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerification {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String verificationType;
    private String documentNumber;
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;

    private String verifiedBy;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.submittedAt == null) {
            this.submittedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}