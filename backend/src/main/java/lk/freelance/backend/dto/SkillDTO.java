package lk.freelance.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    private UUID skillId;
    private String skillName;
    private String category;

    // FIX: Add this field so the builder methods can resolve
    private LocalDateTime createdAt;

    // Fields required for UserSkill mapping
    private String proficiencyLevel;
    private Integer yearsOfExperience;
}