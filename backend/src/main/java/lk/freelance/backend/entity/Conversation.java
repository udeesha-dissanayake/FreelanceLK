package lk.freelance.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages;

    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}