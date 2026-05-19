package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 generator
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_deliverables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeliverable {

    @Id
    @GeneratedValue // Auto-detects UUID type
    @UuidGenerator  // Modern replacement for GenericGenerator
    @Column(name = "deliverable_id")
    private UUID deliverableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.deliveredAt == null) {
            this.deliveredAt = LocalDateTime.now();
        }
    }
}