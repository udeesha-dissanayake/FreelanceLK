package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.JobApplicationDTO;
import lk.freelance.backend.dto.JobApplicationRequest;
import lk.freelance.backend.dto.UserSummaryDTO;
import lk.freelance.backend.entity.JobApplication;
import lk.freelance.backend.entity.Listing;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.entity.UserProfile;
import lk.freelance.backend.enums.ApplicationStatus;
import lk.freelance.backend.enums.ListingType;
import lk.freelance.backend.enums.UserRole;
import lk.freelance.backend.exception.BadRequestException;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.exception.UnauthorizedException;
import lk.freelance.backend.repository.JobApplicationRepository;
import lk.freelance.backend.repository.ListingRepository;
import lk.freelance.backend.repository.UserRepository;
import lk.freelance.backend.service.JobApplicationService;
import lk.freelance.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public JobApplicationDTO apply(UUID freelancerId, UUID listingId, JobApplicationRequest request) {
        User freelancer = userRepository.findByIdWithProfile(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found"));

        if (freelancer.getRole() != UserRole.FREELANCER) {
            throw new UnauthorizedException("Only freelancers can apply to jobs");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job listing not found"));

        if (listing.getListingType() != ListingType.PART_TIME_JOB) {
            throw new BadRequestException("This listing is not a job. Use the order flow for gigs.");
        }

        if (listing.getUser().getUserId().equals(freelancerId)) {
            throw new UnauthorizedException("You cannot apply to your own listing");
        }

        if (applicationRepository.existsByListing_ListingIdAndFreelancer_UserId(listingId, freelancerId)) {
            throw new BadRequestException("You have already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .listing(listing)
                .freelancer(freelancer)
                .coverLetter(request.getCoverLetter())
                .expectedRate(request.getExpectedRate())
                .status(ApplicationStatus.PENDING)
                .build();

        application = applicationRepository.save(application);

        notificationService.send(
                listing.getUser(),
                "JOB_APPLICATION",
                "New application received",
                getDisplayName(freelancer) + " applied to your job: " + listing.getTitle(),
                application.getApplicationId()
        );

        return toDTO(application);
    }

    @Override
    @Transactional
    public JobApplicationDTO withdraw(UUID freelancerId, UUID applicationId) {
        JobApplication application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!application.getFreelancer().getUserId().equals(freelancerId)) {
            throw new UnauthorizedException("This is not your application");
        }
        if (application.getStatus() == ApplicationStatus.ACCEPTED) {
            throw new BadRequestException("Cannot withdraw an already accepted application");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        return toDTO(applicationRepository.save(application));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getApplicants(UUID clientId, UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job listing not found"));

        if (!listing.getUser().getUserId().equals(clientId)) {
            throw new UnauthorizedException("This is not your job listing");
        }

        return applicationRepository.findByListingIdWithFreelancerProfile(listingId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobApplicationDTO accept(UUID clientId, UUID applicationId) {
        JobApplication application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        Listing listing = application.getListing();

        if (!listing.getUser().getUserId().equals(clientId)) {
            throw new UnauthorizedException("This is not your job listing");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Application is already " + application.getStatus().name().toLowerCase());
        }

        application.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);

        // Auto-reject all other pending applicants
        applicationRepository.findByListingIdAndStatus(listing.getListingId(), ApplicationStatus.PENDING)
                .stream()
                .filter(o -> !o.getApplicationId().equals(applicationId))
                .forEach(other -> {
                    other.setStatus(ApplicationStatus.REJECTED);
                    applicationRepository.save(other);
                    notificationService.send(other.getFreelancer(), "JOB_APPLICATION",
                            "Application not selected",
                            "Your application for \"" + listing.getTitle() + "\" was not selected.",
                            other.getApplicationId());
                });

        notificationService.send(application.getFreelancer(), "JOB_APPLICATION",
                "Application accepted!",
                "Congratulations! Your application for \"" + listing.getTitle() + "\" has been accepted.",
                application.getApplicationId());

        return toDTO(application);
    }

    @Override
    @Transactional
    public JobApplicationDTO reject(UUID clientId, UUID applicationId) {
        JobApplication application = applicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        Listing listing = application.getListing();

        if (!listing.getUser().getUserId().equals(clientId)) {
            throw new UnauthorizedException("This is not your job listing");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Application is already " + application.getStatus().name().toLowerCase());
        }

        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);

        notificationService.send(application.getFreelancer(), "JOB_APPLICATION",
                "Application not selected",
                "Your application for \"" + listing.getTitle() + "\" was not selected.",
                application.getApplicationId());

        return toDTO(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationDTO> getMyApplications(UUID freelancerId) {
        return applicationRepository.findByFreelancerIdWithDetails(freelancerId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── helpers ────────────────────────────────────────────────────

    private String getDisplayName(User user) {
        UserProfile p = user.getUserProfile();
        if (p == null) return "A freelancer";
        if (p.getDisplayName() != null && !p.getDisplayName().isBlank()) return p.getDisplayName();
        String full = ((p.getFirstName() != null ? p.getFirstName() : "") + " "
                + (p.getLastName() != null ? p.getLastName() : "")).trim();
        return full.isBlank() ? "A freelancer" : full;
    }

    private JobApplicationDTO toDTO(JobApplication app) {
        User f = app.getFreelancer();
        UserProfile p = f.getUserProfile();
        return JobApplicationDTO.builder()
                .applicationId(app.getApplicationId())
                .listingId(app.getListing().getListingId())
                .jobTitle(app.getListing().getTitle())
                .freelancer(UserSummaryDTO.builder()
                        .userId(f.getUserId())
                        .firstName(p != null ? p.getFirstName() : null)
                        .lastName(p != null ? p.getLastName() : null)
                        .avatarUrl(p != null ? p.getAvatarUrl() : null)
                        .build())
                .coverLetter(app.getCoverLetter())
                .expectedRate(app.getExpectedRate())
                .status(app.getStatus())
                .appliedAt(app.getCreatedAt())
                .build();
    }
}
