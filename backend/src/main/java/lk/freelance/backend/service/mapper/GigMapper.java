package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.GigDetailDTO;
import lk.freelance.backend.dto.SellerDetailDTO; // Correct Import
import lk.freelance.backend.dto.SkillDTO;
import lk.freelance.backend.entity.Gig;
import lk.freelance.backend.entity.Listing;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.entity.UserProfile;
import lk.freelance.backend.entity.TrustScore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GigMapper {

    public GigDetailDTO toDetailDTO(Gig gig, Listing listing, User user) {
        if (gig == null || listing == null || user == null) {
            return null;
        }

        UserProfile profile = user.getUserProfile();
        TrustScore trustScore = user.getTrustScore();

        return GigDetailDTO.builder()
                .gigId(gig.getGigId())
                .listingId(listing.getListingId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .category(gig.getCategory())
                .basePrice(gig.getBasePrice())
                .deliveryDays(gig.getDeliveryDays())

                // 1. Map Gig/Listing Skills
                .requiredSkills(listing.getSkills() != null ? listing.getSkills().stream()
                        .map(ls -> SkillDTO.builder()
                                .skillId(ls.getSkill().getSkillId())
                                .skillName(ls.getSkill().getSkillName())
                                .category(ls.getSkill().getCategory())
                                .build())
                        .collect(Collectors.toList()) : List.of())

                // 2. Map Seller Details (Strictly matching your DTO)
                .seller(SellerDetailDTO.builder()
                        .userId(user.getUserId())
                        .firstName(profile != null ? profile.getFirstName() : null)
                        .lastName(profile != null ? profile.getLastName() : null)
                        .displayName(profile != null ? profile.getDisplayName() : "User")
                        .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                        .bio(profile != null ? profile.getBio() : null)
                        .location(profile != null ? profile.getLocation() : null)
                        .memberSince(user.getCreatedAt())

                        // Trust Score mappings (Null safe)
                        .trustScore(trustScore != null ? trustScore.getOverallScore() : java.math.BigDecimal.ZERO)
                        .averageRating(trustScore != null ? trustScore.getReviewScore() : java.math.BigDecimal.ZERO)
                        .completionRate(trustScore != null ? trustScore.getCompletionRate() : java.math.BigDecimal.ZERO)

                        // Default these to 0 if they aren't in your TrustScore entity yet
                        .totalReviews(0)
                        .totalCompletedOrders(0)

                        // Map User Skills if available (User -> UserSkills)
                        .skills(user.getUserSkills() != null ? user.getUserSkills().stream()
                                .map(us -> SkillDTO.builder()
                                        .skillId(us.getSkill().getSkillId())
                                        .skillName(us.getSkill().getSkillName())
                                        .build())
                                .collect(Collectors.toList()) : null)
                        .build())
                .build();
    }
}