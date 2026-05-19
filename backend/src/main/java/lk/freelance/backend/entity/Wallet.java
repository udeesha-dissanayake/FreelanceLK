package lk.freelance.backend.entity;

// Removed unused import: lk.freelance.backend.enums.PaymentStatus;
import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern replacement
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class Wallet {

    @Id
    @GeneratedValue
    @UuidGenerator // Modern Hibernate 6.5+ generator
    @Column(name = "wallet_id")
    private UUID walletId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "pending_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal pendingBalance;

    @Column(length = 3)
    private String currency; // e.g., LKR, USD

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Ensure financial fields aren't null on creation
        if (this.balance == null) this.balance = BigDecimal.ZERO;
        if (this.pendingBalance == null) this.pendingBalance = BigDecimal.ZERO;
        if (this.currency == null) this.currency = "LKR";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}