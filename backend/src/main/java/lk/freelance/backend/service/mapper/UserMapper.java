package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.SkillDTO;
import lk.freelance.backend.dto.UserProfileDTO;
import lk.freelance.backend.dto.TrustScoreDTO;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.entity.UserProfile;
import lk.freelance.backend.entity.TrustScore;
import lk.freelance.backend.entity.UserSkill;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    // Overload with skills — used by getUserProfile / updateUserProfile
    public UserProfileDTO toUserProfileDTO(User user, UserProfile profile, TrustScore trustScore, List<UserSkill> skills) {
        if (user == null) return null;

        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .displayName(profile != null ? profile.getDisplayName() : "User")
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .location(profile != null ? profile.getLocation() : null)
                .timezone(profile != null ? profile.getTimezone() : null)
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .createdAt(user.getCreatedAt())
                .trustScore(trustScore != null ? toTrustScoreDTO(trustScore) : null)
                .skills(skills != null ? skills.stream()
                        .map(s -> SkillDTO.builder()
                                .skillId(s.getSkill().getSkillId())
                                .skillName(s.getSkill().getSkillName())
                                .category(s.getSkill().getCategory())
                                .proficiencyLevel(String.valueOf(s.getProficiencyLevel()))
                                .yearsOfExperience(s.getYearsOfExperience() != null ? s.getYearsOfExperience().intValue() : null)
                                .createdAt(s.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }

    // Original 3-param method — used by AuthServiceImpl and other callers, skills will be null
    public UserProfileDTO toUserProfileDTO(User user, UserProfile profile, TrustScore trustScore) {
        if (user == null) return null;

        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .displayName(profile != null ? profile.getDisplayName() : "User")
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .location(profile != null ? profile.getLocation() : null)
                .timezone(profile != null ? profile.getTimezone() : null)
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .createdAt(user.getCreatedAt())
                .trustScore(trustScore != null ? toTrustScoreDTO(trustScore) : null)
                .build();
    }

    public TrustScoreDTO toTrustScoreDTO(TrustScore trustScore) {
        // FIX: Match the fields from your TrustScore entity and TrustScoreDTO
        return TrustScoreDTO.builder()
                .overallScore(trustScore.getOverallScore())
                .reviewScore(trustScore.getReviewScore()) // Changed from ratingScore
                .verificationScore(trustScore.getVerificationScore())
                .transactionConsistencyScore(trustScore.getTransactionConsistencyScore())
                .completionRate(trustScore.getCompletionRate()) // Changed from completionScore
                .responseTimeScore(trustScore.getResponseTimeScore()) // Changed from responseScore
                .updatedAt(trustScore.getUpdatedAt())
                .build();
    }
}