// ================================================================
// ADD SKILL REQUEST DTO
// FreelanceLK.com - Skill Management
// ================================================================
// Request body for adding a skill to user profile
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for adding skill request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(max = 100)
    private String skillName;

    @Size(max = 100)
    private String category;

    @Min(value = 1, message = "Proficiency level must be between 1 and 5")
    @Max(value = 5, message = "Proficiency level must be between 1 and 5")
    private Integer proficiencyLevel;

    @DecimalMin(value = "0.0", message = "Years of experience must be positive")
    @DecimalMax(value = "50.0", message = "Years of experience must not exceed 50")
    private BigDecimal yearsOfExperience;
}