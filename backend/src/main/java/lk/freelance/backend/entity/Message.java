package lk.freelance.backend.entity;

import jakarta.persistence.*;
import lk.freelance.backend.enums.MessageType;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.isRead == null) this.isRead = false;
        if (this.messageType == null) this.messageType = MessageType.TEXT;
    }
}