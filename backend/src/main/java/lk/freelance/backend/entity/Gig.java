package lk.freelance.backend.entity;

import lk.freelance.backend.enums.PricingModel;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gigs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gig {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID gigId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model")
    private PricingModel pricingModel;

    private BigDecimal basePrice;
    private Integer deliveryDays;
    private Integer revisionsIncluded;
    private String category;
    private String subcategory;

    @Column(columnDefinition = "TEXT")
    private String requirements;

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