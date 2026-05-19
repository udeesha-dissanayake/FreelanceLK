package lk.freelance.backend.entity;

import lk.freelance.backend.enums.ApplicationStatus;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"listing_id", "freelancer_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private User freelancer;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private String expectedRate; // freelancer's proposed rate (optional)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = ApplicationStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
