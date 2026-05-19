package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 generator
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listing_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingSkill {

    @Id
    @GeneratedValue // Auto-detects UUID type
    @UuidGenerator  // Modern replacement for GenericGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isRequired == null) {
            this.isRequired = true; // Defaulting to true for required skills
        }
    }
}