package lk.freelance.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "withdrawal_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID withdrawalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal amount;
    private String status;

    // FIX: Add bank account details map
    @ElementCollection
    @CollectionTable(name = "withdrawal_bank_details", joinColumns = @JoinColumn(name = "withdrawal_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value")
    private Map<String, String> bankAccountDetails;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    // FIX: Add missing metadata fields
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
        if (this.requestedAt == null) this.requestedAt = now;
        if (this.status == null) this.status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}