package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 replacement
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue
    @UuidGenerator // Clean, modern replacement for GenericGenerator
    @Column(name = "session_id")
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "login_at", updatable = false)
    private LocalDateTime loginAt;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.loginAt = now;
        this.lastActivity = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastActivity = LocalDateTime.now();
    }
}