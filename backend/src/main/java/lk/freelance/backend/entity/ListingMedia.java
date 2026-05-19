package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 generator
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "listing_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingMedia {

    @Id
    @GeneratedValue // Auto-detects UUID
    @UuidGenerator  // Simple replacement for GenericGenerator
    @Column(name = "media_id")
    private UUID mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "media_type")
    private String mediaType; // IMAGE, VIDEO

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isPrimary == null) {
            this.isPrimary = false;
        }
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }
}