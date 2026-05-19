package lk.freelance.backend.service.mapper;



import lk.freelance.backend.dto.*;

import lk.freelance.backend.entity.*;

import org.springframework.stereotype.Component;



import java.util.Comparator;

import java.util.List;

import java.util.stream.Collectors;



@Component

public class JobMapper {



    public JobDetailDTO toDetailDTO(PartTimeJob job, Listing listing, User user) {

// FIX: Mapping nested SellerDetailDTO

        SellerDetailDTO sellerDetail = SellerDetailDTO.builder()

                .userId(user.getUserId())

                .displayName(user.getUserProfile() != null ? user.getUserProfile().getDisplayName() : "User")

                .avatarUrl(user.getUserProfile() != null ? user.getUserProfile().getAvatarUrl() : null)

                .trustScore(user.getTrustScore() != null ? user.getTrustScore().getOverallScore() : java.math.BigDecimal.ZERO)

                .build();



        return JobDetailDTO.builder()

                .jobId(job.getJobId())

                .listingId(listing.getListingId())

                .title(listing.getTitle())

                .description(listing.getDescription())

                .status(listing.getStatus().name())

                .viewsCount(listing.getViewsCount())

                .isFeatured(listing.getIsFeatured())

                .createdAt(listing.getCreatedAt())

// Ensure your JobDetailDTO has 'updatedAt' field

                .employmentType(job.getEmploymentType().name())

                .hourlyRate(job.getHourlyRate())

                .monthlySalary(job.getMonthlySalary())

                .hoursPerWeek(job.getHoursPerWeek())

                .durationMonths(job.getDurationMonths())

                .startDate(job.getStartDate())

                .endDate(job.getEndDate())

                .location(job.getLocation())

                .isRemote(job.getIsRemote())

                .requirements(job.getRequirements())

                .responsibilities(job.getResponsibilities())

// Use nested object

                .seller(sellerDetail)

                .mediaUrls(listing.getMedia() != null ? listing.getMedia().stream()

                        .sorted(Comparator.comparingInt(ListingMedia::getDisplayOrder))

                        .map(ListingMedia::getMediaUrl)

                        .collect(Collectors.toList()) : List.of())

                .build();

    }



    public JobSummaryDTO toSummaryDTO(PartTimeJob job, Listing listing) {

        User user = listing.getUser();



// FIX: Mapping nested SellerSummaryDTO

        SellerSummaryDTO sellerSummary = SellerSummaryDTO.builder()

                .userId(user.getUserId())

                .displayName(user.getUserProfile() != null ? user.getUserProfile().getDisplayName() : "User")

                .avatarUrl(user.getUserProfile() != null ? user.getUserProfile().getAvatarUrl() : null)

                .trustScore(user.getTrustScore() != null ? user.getTrustScore().getOverallScore() : java.math.BigDecimal.ZERO)

                .build();



        return JobSummaryDTO.builder()

                .jobId(job.getJobId())

                .listingId(listing.getListingId())

                .title(listing.getTitle())

                .status(listing.getStatus().name())

                .viewsCount(listing.getViewsCount())

                .isFeatured(listing.getIsFeatured())

                .createdAt(listing.getCreatedAt())

                .employmentType(job.getEmploymentType().name())

                .hourlyRate(job.getHourlyRate())

                .monthlySalary(job.getMonthlySalary())

                .location(job.getLocation())

                .isRemote(job.getIsRemote())

                .seller(sellerSummary)

                .thumbnailUrl(listing.getMedia() != null ? listing.getMedia().stream()

                        .filter(ListingMedia::getIsPrimary)

                        .findFirst()

                        .map(ListingMedia::getMediaUrl)

                        .orElse(null) : null)

                .build();

    }

}