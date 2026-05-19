package lk.freelance.backend.entity;

import jakarta.persistence.*;
import lk.freelance.backend.enums.ListingStatus;
import lk.freelance.backend.enums.ListingType;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingType listingType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    private Integer viewsCount;
    private Boolean isFeatured;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingSkill> skills;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingMedia> media;

    @PrePersist
    protected void onCreate() {
        if (viewsCount == null) viewsCount = 0;
        if (isFeatured == null) isFeatured = false;
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }
}