package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 generator
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessage {

    @Id
    @GeneratedValue // Auto-detects UUID type
    @UuidGenerator  // Modern replacement for GenericGenerator
    @Column(name = "message_id")
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isRead == null) {
            this.isRead = false;
        }
    }
}