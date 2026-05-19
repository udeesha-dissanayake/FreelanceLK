package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.entity.*;
import lk.freelance.backend.enums.*;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.exception.UnauthorizedException;
import lk.freelance.backend.repository.*;
import lk.freelance.backend.service.ListingService;
import lk.freelance.backend.service.mapper.GigMapper;
import lk.freelance.backend.service.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final GigRepository gigRepository;
    private final GigPackageRepository gigPackageRepository;
    private final PartTimeJobRepository partTimeJobRepository;
    private final ListingMediaRepository listingMediaRepository;
    private final ListingSkillRepository listingSkillRepository;
    private final SkillRepository skillRepository;
    private final SavedListingRepository savedListingRepository;
    private final UserRepository userRepository;
    private final GigMapper gigMapper;
    private final JobMapper jobMapper;

    // ================================================================
    // GIG — CREATE
    // ================================================================

    @Override
    @Transactional
    public GigDetailDTO createGig(UUID userId, CreateGigRequest request) {
        log.info("Creating gig for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Listing listing = listingRepository.save(Listing.builder()
                .user(user)
                .listingType(ListingType.GIG)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ListingStatus.ACTIVE)
                .build());

        Gig gig = gigRepository.save(Gig.builder()
                .listing(listing)
                .pricingModel(PricingModel.valueOf(request.getPricingModel()))
                .basePrice(request.getBasePrice())
                .deliveryDays(request.getDeliveryDays())
                .revisionsIncluded(request.getRevisionsIncluded())
                .category(request.getCategory())
                .subcategory(request.getSubcategory())
                .requirements(request.getRequirements())
                .build());

        saveMedia(listing, request.getMediaUrls());
        saveSkills(listing, request.getRequiredSkillIds());
        savePackages(gig, request.getPackages());

        return toGigDetailDTO(gig, listing, user);
    }

    // ================================================================
    // GIG — READ
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public GigDetailDTO getGigById(UUID gigId) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new ResourceNotFoundException("Gig", "id", gigId));
        return toGigDetailDTO(gig, gig.getListing(), gig.getListing().getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GigSummaryDTO> getAllGigs(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                                   Integer maxDeliveryDays, String sortBy, Integer page, Integer size) {
        Pageable pageable = buildPageable(page, size, sortBy, "basePrice");

        BigDecimal effectiveMin = minPrice != null ? minPrice : BigDecimal.ZERO;
        BigDecimal effectiveMax = maxPrice != null ? maxPrice : new BigDecimal("9999999");
        int effectiveMaxDays = maxDeliveryDays != null ? maxDeliveryDays : 365;

        Page<Gig> gigPage = gigRepository.findByFilters(category, effectiveMin, effectiveMax, effectiveMaxDays, pageable);
        return toGigPagedResponse(gigPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GigSummaryDTO> getGigsByCategory(String category, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("listing.createdAt").descending());
        return toGigPagedResponse(gigRepository.findByCategory(category, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GigSummaryDTO> getUserGigs(UUID userId, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("listing.createdAt").descending());
        Page<Gig> gigPage;
        if (status != null && !status.isBlank()) {
            gigPage = gigRepository.findByListing_User_UserIdAndListing_Status(
                    userId, ListingStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            gigPage = gigRepository.findByListing_User_UserId(userId, pageable);
        }
        return toGigPagedResponse(gigPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GigSummaryDTO> getFeaturedGigs(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 8, Sort.by("listing.createdAt").descending());
        return gigRepository.findByListingStatus(ListingStatus.ACTIVE, pageable)
                .getContent().stream()
                .filter(g -> Boolean.TRUE.equals(g.getListing().getIsFeatured()))
                .map(g -> toGigSummaryDTO(g, g.getListing()))
                .collect(Collectors.toList());
    }

    // ================================================================
    // GIG — UPDATE / DELETE / STATUS
    // ================================================================

    @Override
    @Transactional
    public GigDetailDTO updateGig(UUID userId, UUID gigId, CreateGigRequest request) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new ResourceNotFoundException("Gig", "id", gigId));
        verifyListingOwner(userId, gig.getListing());

        Listing listing = gig.getListing();
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listingRepository.save(listing);

        gig.setPricingModel(PricingModel.valueOf(request.getPricingModel()));
        gig.setBasePrice(request.getBasePrice());
        gig.setDeliveryDays(request.getDeliveryDays());
        gig.setRevisionsIncluded(request.getRevisionsIncluded());
        gig.setCategory(request.getCategory());
        gig.setSubcategory(request.getSubcategory());
        gig.setRequirements(request.getRequirements());
        gig = gigRepository.save(gig);

        // Replace media and skills
        listingMediaRepository.deleteAllByListing_ListingId(listing.getListingId());
        listingSkillRepository.deleteAllByListing_ListingId(listing.getListingId());
        gigPackageRepository.deleteAllByGig_GigId(gigId);

        saveMedia(listing, request.getMediaUrls());
        saveSkills(listing, request.getRequiredSkillIds());
        savePackages(gig, request.getPackages());

        return toGigDetailDTO(gig, listing, listing.getUser());
    }

    @Override
    @Transactional
    public void deleteGig(UUID userId, UUID gigId) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new ResourceNotFoundException("Gig", "id", gigId));
        verifyListingOwner(userId, gig.getListing());
        gigPackageRepository.deleteAllByGig_GigId(gigId);
        gigRepository.delete(gig);
        listingRepository.delete(gig.getListing());
    }

    @Override
    @Transactional
    public GigDetailDTO publishGig(UUID userId, UUID gigId) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new ResourceNotFoundException("Gig", "id", gigId));
        verifyListingOwner(userId, gig.getListing());
        gig.getListing().setStatus(ListingStatus.ACTIVE);
        listingRepository.save(gig.getListing());
        return toGigDetailDTO(gig, gig.getListing(), gig.getListing().getUser());
    }

    @Override
    @Transactional
    public GigDetailDTO pauseGig(UUID userId, UUID gigId) {
        Gig gig = gigRepository.findById(gigId)
                .orElseThrow(() -> new ResourceNotFoundException("Gig", "id", gigId));
        verifyListingOwner(userId, gig.getListing());
        gig.getListing().setStatus(ListingStatus.PAUSED);
        listingRepository.save(gig.getListing());
        return toGigDetailDTO(gig, gig.getListing(), gig.getListing().getUser());
    }

    // ================================================================
    // JOB — CREATE
    // ================================================================

    @Override
    @Transactional
    public JobDetailDTO createJob(UUID userId, CreateJobRequest request) {
        log.info("Creating part-time job for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Listing listing = listingRepository.save(Listing.builder()
                .user(user)
                .listingType(ListingType.PART_TIME_JOB)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ListingStatus.DRAFT)
                .build());

        PartTimeJob job = partTimeJobRepository.save(PartTimeJob.builder()
                .listing(listing)
                .employmentType(EmploymentType.valueOf(request.getEmploymentType()))
                .hourlyRate(request.getHourlyRate())
                .monthlySalary(request.getMonthlySalary())
                .hoursPerWeek(request.getHoursPerWeek())
                .durationMonths(request.getDurationMonths())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .isRemote(Boolean.TRUE.equals(request.getIsRemote()))
                .requirements(request.getRequirements())
                .responsibilities(request.getResponsibilities())
                .build());

        saveMedia(listing, request.getMediaUrls());
        saveSkills(listing, request.getRequiredSkillIds());

        return jobMapper.toDetailDTO(job, listing, user);
    }

    // ================================================================
    // JOB — READ
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public JobDetailDTO getJobById(UUID jobId) {
        PartTimeJob job = partTimeJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        return jobMapper.toDetailDTO(job, job.getListing(), job.getListing().getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryDTO> getAllJobs(String employmentType, Boolean isRemote, BigDecimal minRate,
                                                   BigDecimal maxRate, String location, String sortBy,
                                                   Integer page, Integer size) {
        Pageable pageable = buildPageable(page, size, sortBy, "listing.createdAt");

        // Apply the most specific filter available
        Page<PartTimeJob> jobPage;
        if (Boolean.TRUE.equals(isRemote)) {
            jobPage = partTimeJobRepository.findRemoteJobs(pageable);
        } else if (location != null && !location.isBlank()) {
            jobPage = partTimeJobRepository.findByLocation(location, pageable);
        } else if (employmentType != null && !employmentType.isBlank()) {
            jobPage = partTimeJobRepository.findByEmploymentType(EmploymentType.valueOf(employmentType.toUpperCase()), pageable);
        } else {
            // Fallback: all active jobs
            jobPage = partTimeJobRepository.findAll(pageable);
        }

        return toJobPagedResponse(jobPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryDTO> getUserJobs(UUID userId, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<PartTimeJob> jobs = partTimeJobRepository.findByUserId(userId);

        // Optional status filter
        if (status != null && !status.isBlank()) {
            ListingStatus ls = ListingStatus.valueOf(status.toUpperCase());
            jobs = jobs.stream()
                    .filter(j -> j.getListing().getStatus() == ls)
                    .collect(Collectors.toList());
        }

        List<JobSummaryDTO> summaries = jobs.stream()
                .map(j -> jobMapper.toSummaryDTO(j, j.getListing()))
                .collect(Collectors.toList());

        return buildManualPagedResponse(summaries, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryDTO> getRemoteJobs(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toJobPagedResponse(partTimeJobRepository.findRemoteJobs(pageable));
    }

    // ================================================================
    // JOB — UPDATE / DELETE
    // ================================================================

    @Override
    @Transactional
    public JobDetailDTO updateJob(UUID userId, UUID jobId, CreateJobRequest request) {
        PartTimeJob job = partTimeJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        verifyListingOwner(userId, job.getListing());

        Listing listing = job.getListing();
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listingRepository.save(listing);

        job.setEmploymentType(EmploymentType.valueOf(request.getEmploymentType()));
        job.setHourlyRate(request.getHourlyRate());
        job.setMonthlySalary(request.getMonthlySalary());
        job.setHoursPerWeek(request.getHoursPerWeek());
        job.setDurationMonths(request.getDurationMonths());
        job.setStartDate(request.getStartDate());
        job.setEndDate(request.getEndDate());
        job.setLocation(request.getLocation());
        job.setIsRemote(Boolean.TRUE.equals(request.getIsRemote()));
        job.setRequirements(request.getRequirements());
        job.setResponsibilities(request.getResponsibilities());
        job = partTimeJobRepository.save(job);

        listingMediaRepository.deleteAllByListing_ListingId(listing.getListingId());
        listingSkillRepository.deleteAllByListing_ListingId(listing.getListingId());
        saveMedia(listing, request.getMediaUrls());
        saveSkills(listing, request.getRequiredSkillIds());

        return jobMapper.toDetailDTO(job, listing, listing.getUser());
    }

    @Override
    @Transactional
    public void deleteJob(UUID userId, UUID jobId) {
        PartTimeJob job = partTimeJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        verifyListingOwner(userId, job.getListing());
        partTimeJobRepository.delete(job);
        listingRepository.delete(job.getListing());
    }

    // ================================================================
    // SEARCH
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<?> searchListings(String query, String listingType, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        if (query == null || query.isBlank()) {
            // Return all active listings of the requested type
            ListingType type = listingType != null ? ListingType.valueOf(listingType.toUpperCase()) : null;
            if (type == ListingType.PART_TIME_JOB) {
                List<JobSummaryDTO> jobs = partTimeJobRepository.findAll().stream()
                        .filter(j -> j.getListing().getStatus() == ListingStatus.ACTIVE)
                        .map(j -> jobMapper.toSummaryDTO(j, j.getListing()))
                        .collect(Collectors.toList());
                return buildManualPagedResponse(jobs, page, size);
            } else {
                List<GigSummaryDTO> gigs = gigRepository.findAll().stream()
                        .filter(g -> g.getListing().getStatus() == ListingStatus.ACTIVE)
                        .map(g -> toGigSummaryDTO(g, g.getListing()))
                        .collect(Collectors.toList());
                return buildManualPagedResponse(gigs, page, size);
            }
        }

        // Full-text search via PostgreSQL tsvector
        String typeParam = listingType != null ? listingType.toUpperCase() : null;
        Page<Listing> results = listingRepository.fullTextSearch(query, typeParam, "ACTIVE", pageable);

        // Resolve to typed DTOs
        List<Object> dtos = results.getContent().stream()
                .map(listing -> {
                    if (listing.getListingType() == ListingType.GIG) {
                        return gigRepository.findByListing_ListingId(listing.getListingId())
                                .map(g -> (Object) toGigSummaryDTO(g, listing))
                                .orElse(null);
                    } else {
                        return partTimeJobRepository.findByListing_ListingId(listing.getListingId())
                                .map(j -> (Object) jobMapper.toSummaryDTO(j, listing))
                                .orElse(null);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PagedResponse.builder()
                .content(dtos)
                .currentPage(results.getNumber())
                .totalPages(results.getTotalPages())
                .totalElements(results.getTotalElements())
                .pageSize(results.getSize())
                .hasNext(results.hasNext())
                .hasPrevious(results.hasPrevious())
                .build();
    }

    // ================================================================
    // SAVED LISTINGS
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<?> getSavedListings(UUID userId, Integer page, Integer size) {
        Page<SavedListing> saved = savedListingRepository.findByUser_UserId(userId,
                PageRequest.of(page, size, Sort.by("savedAt").descending()));

        List<Object> dtos = saved.getContent().stream()
                .map(sl -> {
                    Listing listing = sl.getListing();
                    if (listing.getListingType() == ListingType.GIG) {
                        return gigRepository.findByListing_ListingId(listing.getListingId())
                                .map(g -> (Object) toGigSummaryDTO(g, listing))
                                .orElse(null);
                    } else {
                        return partTimeJobRepository.findByListing_ListingId(listing.getListingId())
                                .map(j -> (Object) jobMapper.toSummaryDTO(j, listing))
                                .orElse(null);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PagedResponse.builder()
                .content(dtos)
                .currentPage(saved.getNumber())
                .totalPages(saved.getTotalPages())
                .totalElements(saved.getTotalElements())
                .pageSize(saved.getSize())
                .hasNext(saved.hasNext())
                .hasPrevious(saved.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public void saveListing(UUID userId, UUID listingId) {
        if (savedListingRepository.existsByUser_UserIdAndListing_ListingId(userId, listingId)) {
            return; // Already saved — idempotent
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", listingId));
        savedListingRepository.save(SavedListing.builder().user(user).listing(listing).build());
    }

    @Override
    @Transactional
    public void unsaveListing(UUID userId, UUID listingId) {
        savedListingRepository.deleteByUser_UserIdAndListing_ListingId(userId, listingId);
    }

    // ================================================================
    // MISC
    // ================================================================

    @Override
    @Transactional
    public void incrementViewCount(UUID listingId) {
        listingRepository.findById(listingId).ifPresent(listing -> {
            listing.setViewsCount((listing.getViewsCount() != null ? listing.getViewsCount() : 0) + 1);
            listingRepository.save(listing);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        // Aggregate categories from active gigs
        List<Gig> allGigs = gigRepository.findAll();
        Map<String, List<String>> categoryMap = new LinkedHashMap<>();

        allGigs.stream()
                .filter(g -> g.getListing().getStatus() == ListingStatus.ACTIVE)
                .forEach(g -> {
                    String cat = g.getCategory();
                    String sub = g.getSubcategory();
                    if (cat != null) {
                        categoryMap.computeIfAbsent(cat, k -> new ArrayList<>());
                        if (sub != null && !categoryMap.get(cat).contains(sub)) {
                            categoryMap.get(cat).add(sub);
                        }
                    }
                });

        return categoryMap.entrySet().stream()
                .map(e -> CategoryDTO.builder()
                        .name(e.getKey())
                        .displayName(capitalize(e.getKey()))
                        .listingCount((int) allGigs.stream()
                                .filter(g -> e.getKey().equals(g.getCategory())
                                        && g.getListing().getStatus() == ListingStatus.ACTIVE)
                                .count())
                        .subcategories(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ListingStatisticsDTO getListingStatistics(UUID userId) {
        List<Gig> gigs = gigRepository.findByUserId(userId);
        List<PartTimeJob> jobs = partTimeJobRepository.findByUserId(userId);

        List<Listing> allListings = new ArrayList<>();
        gigs.forEach(g -> allListings.add(g.getListing()));
        jobs.forEach(j -> allListings.add(j.getListing()));

        int totalViews = allListings.stream()
                .mapToInt(l -> l.getViewsCount() != null ? l.getViewsCount() : 0).sum();

        return ListingStatisticsDTO.builder()
                .totalListings(allListings.size())
                .activeListings((int) allListings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count())
                .draftListings((int) allListings.stream().filter(l -> l.getStatus() == ListingStatus.DRAFT).count())
                .pausedListings((int) allListings.stream().filter(l -> l.getStatus() == ListingStatus.PAUSED).count())
                .totalViews(totalViews)
                .totalOrders(0)   // Wired up in OrderService; placeholder here
                .totalRevenue(BigDecimal.ZERO)
                .build();
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    private void saveMedia(Listing listing, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        for (int i = 0; i < urls.size(); i++) {
            listingMediaRepository.save(ListingMedia.builder()
                    .listing(listing)
                    .mediaUrl(urls.get(i))
                    .mediaType("IMAGE")
                    .displayOrder(i)
                    .isPrimary(i == 0)
                    .build());
        }
    }

    private void saveSkills(Listing listing, List<UUID> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return;
        for (UUID skillId : skillIds) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
            listingSkillRepository.save(ListingSkill.builder()
                    .listing(listing)
                    .skill(skill)
                    .isRequired(true)
                    .build());
        }
    }

    private void savePackages(Gig gig, List<GigPackageRequest> packages) {
        if (packages == null || packages.isEmpty()) return;
        packages.forEach(pkg -> gigPackageRepository.save(GigPackage.builder()
                .gig(gig)
                .packageName(pkg.getPackageName())
                .packageDescription(pkg.getPackageDescription())
                .price(pkg.getPrice())
                .deliveryDays(pkg.getDeliveryDays())
                .revisions(pkg.getRevisions())
                .build()));
    }

    private void verifyListingOwner(UUID userId, Listing listing) {
        if (!listing.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not own this listing");
        }
    }

    private GigDetailDTO toGigDetailDTO(Gig gig, Listing listing, User user) {
        List<GigPackageDTO> packages = gigPackageRepository.findAll().stream()
                .filter(p -> p.getGig().getGigId().equals(gig.getGigId()))
                .map(p -> GigPackageDTO.builder()
                        .packageId(p.getPackageId())
                        .packageName(p.getPackageName())
                        .packageDescription(p.getPackageDescription())
                        .price(p.getPrice())
                        .deliveryDays(p.getDeliveryDays())
                        .revisions(p.getRevisions())
                        .build())
                .collect(Collectors.toList());

        List<String> mediaUrls = listing.getMedia() != null
                ? listing.getMedia().stream()
                .sorted(Comparator.comparingInt(ListingMedia::getDisplayOrder))
                .map(ListingMedia::getMediaUrl)
                .collect(Collectors.toList())
                : List.of();

        GigDetailDTO dto = gigMapper.toDetailDTO(gig, listing, user);
        dto.setPackages(packages);
        dto.setMediaUrls(mediaUrls);
        dto.setStatus(listing.getStatus() != null ? listing.getStatus().name() : null);
        dto.setViewsCount(listing.getViewsCount());
        dto.setIsFeatured(listing.getIsFeatured());
        dto.setCreatedAt(listing.getCreatedAt());
        return dto;
    }

    private GigSummaryDTO toGigSummaryDTO(Gig gig, Listing listing) {
        User user = listing.getUser();
        String thumbnailUrl = listing.getMedia() != null
                ? listing.getMedia().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                .findFirst()
                .map(ListingMedia::getMediaUrl)
                .orElse(null)
                : null;

        return GigSummaryDTO.builder()
                .gigId(gig.getGigId())
                .listingId(listing.getListingId())
                .title(listing.getTitle())
                .category(gig.getCategory())
                .subcategory(gig.getSubcategory())
                .basePrice(gig.getBasePrice())
                .deliveryDays(gig.getDeliveryDays())
                .pricingModel(gig.getPricingModel() != null ? gig.getPricingModel().name() : null)
                .thumbnailUrl(thumbnailUrl)
                .viewsCount(listing.getViewsCount())
                .isFeatured(listing.getIsFeatured())
                .publishedAt(listing.getCreatedAt())
                .seller(SellerSummaryDTO.builder()
                        .userId(user.getUserId())
                        .displayName(user.getUserProfile() != null ? user.getUserProfile().getDisplayName() : "User")
                        .avatarUrl(user.getUserProfile() != null ? user.getUserProfile().getAvatarUrl() : null)
                        .trustScore(user.getTrustScore() != null ? user.getTrustScore().getOverallScore() : BigDecimal.ZERO)
                        .build())
                .build();
    }

    private PagedResponse<GigSummaryDTO> toGigPagedResponse(Page<Gig> page) {
        return PagedResponse.<GigSummaryDTO>builder()
                .content(page.getContent().stream()
                        .map(g -> toGigSummaryDTO(g, g.getListing()))
                        .collect(Collectors.toList()))
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private PagedResponse<JobSummaryDTO> toJobPagedResponse(Page<PartTimeJob> page) {
        return PagedResponse.<JobSummaryDTO>builder()
                .content(page.getContent().stream()
                        .map(j -> jobMapper.toSummaryDTO(j, j.getListing()))
                        .collect(Collectors.toList()))
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private <T> PagedResponse<T> buildManualPagedResponse(List<T> fullList, int page, int size) {
        int total = fullList.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<T> slice = fullList.subList(fromIndex, toIndex);
        int totalPages = (int) Math.ceil((double) total / size);
        return PagedResponse.<T>builder()
                .content(slice)
                .currentPage(page)
                .totalPages(totalPages)
                .totalElements((long) total)
                .pageSize(size)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String defaultField) {
        Sort sort;
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("basePrice").ascending();
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("basePrice").descending();
        } else if ("delivery_asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("deliveryDays").ascending();
        } else {
            sort = Sort.by(defaultField).descending();
        }
        return PageRequest.of(page, size, sort);
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
