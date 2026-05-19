package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "gig_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigPackage {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "package_id")
    private UUID packageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gig_id", nullable = false)
    private Gig gig;

    @Column(nullable = false, length = 50)
    private String packageName; // BASIC, STANDARD, PREMIUM

    @Column(columnDefinition = "TEXT")
    private String packageDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;

    private Integer revisions;

}