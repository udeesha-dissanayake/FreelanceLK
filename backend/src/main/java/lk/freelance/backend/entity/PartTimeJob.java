package lk.freelance.backend.entity;

import lk.freelance.backend.enums.EmploymentType;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "part_time_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartTimeJob {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID jobId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private BigDecimal hourlyRate;
    private BigDecimal monthlySalary;
    private Integer hoursPerWeek;
    private Integer durationMonths;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Boolean isRemote;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String responsibilities;

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