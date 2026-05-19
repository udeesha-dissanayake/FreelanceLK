package lk.freelance.backend.entity;

import jakarta.persistence.*;
import lk.freelance.backend.enums.PaymentStatus; // Ensure this import exists
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id")
    private User payee;

    private BigDecimal amount;
    private String transactionType;

    // FIX: Use the Enum here so setStatus(PaymentStatus.RELEASED) works
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = PaymentStatus.PENDING;
    }
}