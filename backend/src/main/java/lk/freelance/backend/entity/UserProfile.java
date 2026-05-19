package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 replacement
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class UserProfile {

    @Id
    @GeneratedValue
    @UuidGenerator // Modern replacement for GenericGenerator
    @Column(name = "profile_id")
    private UUID profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 255)
    private String location;

    @Column(length = 50)
    private String timezone;

    @Column(length = 10)
    private String language;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Handle defaults here to avoid "Redundant Assignment" warnings
        if (this.timezone == null) this.timezone = "Asia/Colombo";
        if (this.language == null) this.language = "en";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}