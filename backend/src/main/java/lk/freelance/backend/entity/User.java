package lk.freelance.backend.entity;

// --- CRITICAL IMPORTS ---
import lk.freelance.backend.enums.UserRole;
import lk.freelance.backend.enums.UserStatus;
// ------------------------

import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"userProfile", "trustScore", "wallet", "listings", "userSkills"})
@EqualsAndHashCode(exclude = {"userProfile", "trustScore", "wallet", "listings", "userSkills"})
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator // Modern Hibernate 6 replacement for GenericGenerator
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified;

    @Column(name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled;

    // Relationships
    // Note: Removed FetchType.LAZY here as it is ignored on non-owning @OneToOne
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private TrustScore trustScore;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Wallet wallet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Listing> listings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSkill> userSkills;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Handling default values to avoid Redundant Assignment warnings
        if (this.role == null) this.role = UserRole.CLIENT;
        if (this.status == null) this.status = UserStatus.PENDING_VERIFICATION;
        if (this.emailVerified == null) this.emailVerified = false;
        if (this.phoneVerified == null) this.phoneVerified = false;
        if (this.twoFactorEnabled == null) this.twoFactorEnabled = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}