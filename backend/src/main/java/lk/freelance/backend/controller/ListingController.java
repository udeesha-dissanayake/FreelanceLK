// ================================================================
// LISTING CONTROLLER
// FreelanceLK.com - Gigs & Part-time Jobs Management
// ================================================================
// Handles listing creation, retrieval, search, and management
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.service.ListingService;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for listing operations (Gigs & Part-time Jobs)
 * Base URL: /api/v1/listings
 */
@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ListingController {

    private final ListingService listingService;

    // ================================================================
    // GIG ENDPOINTS
    // ================================================================

    /**
     * Create a new gig
     * POST /api/v1/listings/gigs
     *
     * @param currentUser Authenticated user
     * @param request Gig details
     * @return Created gig details
     */
    @PostMapping("/gigs")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GigDetailDTO>> createGig(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateGigRequest request
    ) {
        GigDetailDTO gig = listingService.createGig(currentUser.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Gig created successfully! You can now publish it.",
                        gig
                ));
    }

    /**
     * Get all active gigs with pagination and filtering
     * GET /api/v1/listings/gigs
     *
     * @param category Filter by category
     * @param minPrice Minimum price filter
     * @param maxPrice Maximum price filter
     * @param maxDeliveryDays Maximum delivery days
     * @param sortBy Sort option
     * @param page Page number
     * @param size Page size
     * @return Paginated list of gigs
     */
    @GetMapping("/gigs")
    public ResponseEntity<ApiResponse<PagedResponse<GigSummaryDTO>>> getAllGigs(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer maxDeliveryDays,
            @RequestParam(required = false, defaultValue = "NEWEST") String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<GigSummaryDTO> gigs = listingService.getAllGigs(
                category,
                minPrice,
                maxPrice,
                maxDeliveryDays,
                sortBy,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(gigs));
    }

    /**
     * Get gig details by ID
     * GET /api/v1/listings/gigs/{gigId}
     *
     * @param gigId Gig ID
     * @return Detailed gig information
     */
    @GetMapping("/gigs/{gigId}")
    public ResponseEntity<ApiResponse<GigDetailDTO>> getGigById(
            @PathVariable UUID gigId
    ) {
        GigDetailDTO gig = listingService.getGigById(gigId);

        // Increment view count asynchronously
        listingService.incrementViewCount(gigId);

        return ResponseEntity
                .ok(ApiResponse.success(gig));
    }

    /**
     * Update gig
     * PUT /api/v1/listings/gigs/{gigId}
     *
     * @param currentUser Authenticated user
     * @param gigId Gig ID
     * @param request Updated gig details
     * @return Updated gig
     */
    @PutMapping("/gigs/{gigId}")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GigDetailDTO>> updateGig(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID gigId,
            @Valid @RequestBody CreateGigRequest request
    ) {
        GigDetailDTO gig = listingService.updateGig(
                currentUser.getUserId(),
                gigId,
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success("Gig updated successfully!", gig));
    }

    /**
     * Delete gig
     * DELETE /api/v1/listings/gigs/{gigId}
     *
     * @param currentUser Authenticated user
     * @param gigId Gig ID
     * @return Success message
     */
    @DeleteMapping("/gigs/{gigId}")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteGig(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID gigId
    ) {
        listingService.deleteGig(currentUser.getUserId(), gigId);

        return ResponseEntity
                .ok(ApiResponse.success("Gig deleted successfully!"));
    }

    /**
     * Publish gig (change status from DRAFT to ACTIVE)
     * POST /api/v1/listings/gigs/{gigId}/publish
     *
     * @param currentUser Authenticated user
     * @param gigId Gig ID
     * @return Updated gig
     */
    @PostMapping("/gigs/{gigId}/publish")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GigDetailDTO>> publishGig(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID gigId
    ) {
        GigDetailDTO gig = listingService.publishGig(currentUser.getUserId(), gigId);

        return ResponseEntity
                .ok(ApiResponse.success("Gig published successfully!", gig));
    }

    /**
     * Pause gig
     * POST /api/v1/listings/gigs/{gigId}/pause
     *
     * @param currentUser Authenticated user
     * @param gigId Gig ID
     * @return Updated gig
     */
    @PostMapping("/gigs/{gigId}/pause")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GigDetailDTO>> pauseGig(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID gigId
    ) {
        GigDetailDTO gig = listingService.pauseGig(currentUser.getUserId(), gigId);

        return ResponseEntity
                .ok(ApiResponse.success("Gig paused successfully!", gig));
    }

    /**
     * Get user's own gigs
     * GET /api/v1/listings/gigs/my-gigs
     *
     * @param currentUser Authenticated user
     * @param status Filter by status
     * @param page Page number
     * @param size Page size
     * @return User's gigs
     */
    @GetMapping("/gigs/my-gigs")
    @PreAuthorize("hasAnyRole('FREELANCER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<GigSummaryDTO>>> getMyGigs(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<GigSummaryDTO> gigs = listingService.getUserGigs(
                currentUser.getUserId(),
                status,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(gigs));
    }

    /**
     * Get gigs by category
     * GET /api/v1/listings/gigs/category/{category}
     *
     * @param category Category name
     * @param page Page number
     * @param size Page size
     * @return Gigs in category
     */
    @GetMapping("/gigs/category/{category}")
    public ResponseEntity<ApiResponse<PagedResponse<GigSummaryDTO>>> getGigsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<GigSummaryDTO> gigs = listingService.getGigsByCategory(
                category,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(gigs));
    }

    /**
     * Get featured gigs
     * GET /api/v1/listings/gigs/featured
     *
     * @param limit Number of featured gigs
     * @return Featured gigs
     */
    @GetMapping("/gigs/featured")
    public ResponseEntity<ApiResponse<List<GigSummaryDTO>>> getFeaturedGigs(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<GigSummaryDTO> gigs = listingService.getFeaturedGigs(limit);

        return ResponseEntity
                .ok(ApiResponse.success(gigs));
    }

    // ================================================================
    // PART-TIME JOB ENDPOINTS
    // ================================================================

    /**
     * Create a new part-time job
     * POST /api/v1/listings/jobs
     *
     * @param currentUser Authenticated user
     * @param request Job details
     * @return Created job details
     */
    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobDetailDTO>> createJob(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateJobRequest request
    ) {
        JobDetailDTO job = listingService.createJob(currentUser.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Job posted successfully!",
                        job
                ));
    }

    /**
     * Get all active jobs with pagination and filtering
     * GET /api/v1/listings/jobs
     *
     * @param employmentType Filter by employment type
     * @param isRemote Remote only filter
     * @param minRate Minimum hourly rate
     * @param maxRate Maximum hourly rate
     * @param location Location filter
     * @param sortBy Sort option
     * @param page Page number
     * @param size Page size
     * @return Paginated list of jobs
     */
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PagedResponse<JobSummaryDTO>>> getAllJobs(
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) Boolean isRemote,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "NEWEST") String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<JobSummaryDTO> jobs = listingService.getAllJobs(
                employmentType,
                isRemote,
                minRate,
                maxRate,
                location,
                sortBy,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(jobs));
    }

    /**
     * Get job details by ID
     * GET /api/v1/listings/jobs/{jobId}
     *
     * @param jobId Job ID
     * @return Detailed job information
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<JobDetailDTO>> getJobById(
            @PathVariable UUID jobId
    ) {
        JobDetailDTO job = listingService.getJobById(jobId);

        // Increment view count asynchronously
        listingService.incrementViewCount(jobId);

        return ResponseEntity
                .ok(ApiResponse.success(job));
    }

    /**
     * Update job
     * PUT /api/v1/listings/jobs/{jobId}
     *
     * @param currentUser Authenticated user
     * @param jobId Job ID
     * @param request Updated job details
     * @return Updated job
     */
    @PutMapping("/jobs/{jobId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobDetailDTO>> updateJob(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID jobId,
            @Valid @RequestBody CreateJobRequest request
    ) {
        JobDetailDTO job = listingService.updateJob(
                currentUser.getUserId(),
                jobId,
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success("Job updated successfully!", job));
    }

    /**
     * Delete job
     * DELETE /api/v1/listings/jobs/{jobId}
     *
     * @param currentUser Authenticated user
     * @param jobId Job ID
     * @return Success message
     */
    @DeleteMapping("/jobs/{jobId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID jobId
    ) {
        listingService.deleteJob(currentUser.getUserId(), jobId);

        return ResponseEntity
                .ok(ApiResponse.success("Job deleted successfully!"));
    }

    /**
     * Get user's posted jobs
     * GET /api/v1/listings/jobs/my-jobs
     *
     * @param currentUser Authenticated user
     * @param status Filter by status
     * @param page Page number
     * @param size Page size
     * @return User's jobs
     */
    @GetMapping("/jobs/my-jobs")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<JobSummaryDTO>>> getMyJobs(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<JobSummaryDTO> jobs = listingService.getUserJobs(
                currentUser.getUserId(),
                status,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(jobs));
    }

    /**
     * Get remote jobs only
     * GET /api/v1/listings/jobs/remote
     *
     * @param page Page number
     * @param size Page size
     * @return Remote jobs
     */
    @GetMapping("/jobs/remote")
    public ResponseEntity<ApiResponse<PagedResponse<JobSummaryDTO>>> getRemoteJobs(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<JobSummaryDTO> jobs = listingService.getRemoteJobs(page, size);

        return ResponseEntity
                .ok(ApiResponse.success(jobs));
    }

    // ================================================================
    // COMMON LISTING ENDPOINTS
    // ================================================================

    /**
     * Search listings (both gigs and jobs)
     * GET /api/v1/listings/search
     *
     * @param query Search query
     * @param listingType Filter by listing type
     * @param page Page number
     * @param size Page size
     * @return Search results
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<?>>> searchListings(
            @RequestParam String query,
            @RequestParam(required = false) String listingType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<?> results = listingService.searchListings(
                query,
                listingType,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(results));
    }

    /**
     * Get saved/favorite listings for current user
     * GET /api/v1/listings/saved
     *
     * @param currentUser Authenticated user
     * @param page Page number
     * @param size Page size
     * @return Saved listings
     */
    @GetMapping("/saved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<?>>> getSavedListings(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<?> savedListings = listingService.getSavedListings(
                currentUser.getUserId(),
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(savedListings));
    }

    /**
     * Save/favorite a listing
     * POST /api/v1/listings/{listingId}/save
     *
     * @param currentUser Authenticated user
     * @param listingId Listing ID
     * @return Success message
     */
    @PostMapping("/{listingId}/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> saveListing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID listingId
    ) {
        listingService.saveListing(currentUser.getUserId(), listingId);

        return ResponseEntity
                .ok(ApiResponse.success("Listing saved successfully!"));
    }

    /**
     * Unsave/unfavorite a listing
     * DELETE /api/v1/listings/{listingId}/save
     *
     * @param currentUser Authenticated user
     * @param listingId Listing ID
     * @return Success message
     */
    @DeleteMapping("/{listingId}/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> unsaveListing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID listingId
    ) {
        listingService.unsaveListing(currentUser.getUserId(), listingId);

        return ResponseEntity
                .ok(ApiResponse.success("Listing removed from saved items!"));
    }

    /**
     * Get all available categories
     * GET /api/v1/listings/categories
     *
     * @return List of categories with counts
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories() {
        List<CategoryDTO> categories = listingService.getAllCategories();

        return ResponseEntity
                .ok(ApiResponse.success(categories));
    }

    /**
     * Get listing statistics (for seller/employer dashboard)
     * GET /api/v1/listings/stats
     *
     * @param currentUser Authenticated user
     * @return Listing statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ListingStatisticsDTO>> getListingStats(
            @CurrentUser UserPrincipal currentUser
    ) {
        ListingStatisticsDTO stats = listingService.getListingStatistics(
                currentUser.getUserId()
        );

        return ResponseEntity
                .ok(ApiResponse.success(stats));
    }
}