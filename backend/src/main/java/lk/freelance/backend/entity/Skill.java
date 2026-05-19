package lk.freelance.backend.entity;

import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Modern replacement
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue
    @UuidGenerator // Replaces the deprecated GenericGenerator
    @Column(name = "skill_id")
    private UUID skillId;

    @Column(name = "skill_name", nullable = false, unique = true)
    private String skillName;

    @Column(name = "category")
    private String category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}