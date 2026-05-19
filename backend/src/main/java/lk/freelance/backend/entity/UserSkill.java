package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern Hibernate 6 replacement
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill {

    @Id
    @GeneratedValue
    @UuidGenerator // Modern replacement for GenericGenerator
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "proficiency_level")
    private Integer proficiencyLevel;

    @Column(name = "years_of_experience", precision = 4, scale = 1)
    private BigDecimal yearsOfExperience;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.proficiencyLevel == null) {
            this.proficiencyLevel = 1; // Default to beginner
        }
    }
}