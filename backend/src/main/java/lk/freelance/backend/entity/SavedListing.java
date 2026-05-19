package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern replacement
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "saved_listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedListing {

    @Id
    @GeneratedValue // Auto-detects UUID type
    @UuidGenerator  // Clean Hibernate 6.5+ replacement
    @Column(name = "saved_id")
    private UUID savedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
}