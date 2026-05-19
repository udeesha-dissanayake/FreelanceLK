package lk.freelance.backend.entity;

// --- ADD THESE IMPORTS ---
import lk.freelance.backend.enums.OrderStatus;
import lk.freelance.backend.enums.PaymentStatus;
// -------------------------

import lombok.*;
// ... rest of your imports
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator; // Hibernate 6 way
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue // Auto-detects UUID
    @UuidGenerator  // Modern Hibernate 6 generator
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // Removed default assignment here, moved to @PrePersist

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "platform_fee", precision = 10, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDeliverable> deliverables = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderMessage> messages = new ArrayList<>();

    @OneToOne(mappedBy = "order")
    private Review review;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.PENDING;
        if (this.paymentStatus == null) this.paymentStatus = PaymentStatus.PENDING;
        if (this.platformFee == null) this.platformFee = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}