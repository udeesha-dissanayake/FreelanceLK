// ================================================================
// USER PROFILE DTO
// FreelanceLK.com - User Profile Information
// ================================================================
// Complete user profile information
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user profile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private UUID userId;
    private String email;
    private String role;
    private String status;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private String location;
    private String timezone;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime createdAt;
    private TrustScoreDTO trustScore;
    private List<SkillDTO> skills;
}