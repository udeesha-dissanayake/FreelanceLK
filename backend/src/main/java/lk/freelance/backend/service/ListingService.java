package lk.freelance.backend.service;

import lk.freelance.backend.dto.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ListingService {
    // Gig Methods
    GigDetailDTO createGig(UUID userId, CreateGigRequest request);

    // Fixed: Matches the 8 arguments the Controller is sending
    PagedResponse<GigSummaryDTO> getAllGigs(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                            Integer maxDeliveryDays, String sortBy, Integer page, Integer size);

    GigDetailDTO getGigById(UUID gigId);

    // Fixed: Using CreateGigRequest as per your Controller
    GigDetailDTO updateGig(UUID userId, UUID gigId, CreateGigRequest request);

    void deleteGig(UUID userId, UUID gigId);

    GigDetailDTO publishGig(UUID userId, UUID gigId);

    GigDetailDTO pauseGig(UUID userId, UUID gigId); // Added missing method

    PagedResponse<GigSummaryDTO> getUserGigs(UUID userId, String status, Integer page, Integer size); // Added missing method

    PagedResponse<GigSummaryDTO> getGigsByCategory(String category, Integer page, Integer size); // Added missing method

    List<GigSummaryDTO> getFeaturedGigs(Integer limit); // Added missing method

    // Part-time Job Methods
    JobDetailDTO createJob(UUID userId, CreateJobRequest request);

    // Fixed: Argument count match
    PagedResponse<JobSummaryDTO> getAllJobs(String employmentType, Boolean isRemote, BigDecimal minRate,
                                            BigDecimal maxRate, String location, String sortBy, Integer page, Integer size);

    JobDetailDTO getJobById(UUID jobId);

    // Fixed: Using CreateJobRequest as per your Controller
    JobDetailDTO updateJob(UUID userId, UUID jobId, CreateJobRequest request);

    void deleteJob(UUID userId, UUID jobId);

    PagedResponse<JobSummaryDTO> getUserJobs(UUID userId, String status, Integer page, Integer size); // Added missing method

    PagedResponse<JobSummaryDTO> getRemoteJobs(Integer page, Integer size); // Added missing method

    // Common Methods
    void incrementViewCount(UUID listingId);

    PagedResponse<?> searchListings(String query, String listingType, Integer page, Integer size);

    PagedResponse<?> getSavedListings(UUID userId, Integer page, Integer size); // Added missing method

    void saveListing(UUID userId, UUID listingId); // Added missing method

    void unsaveListing(UUID userId, UUID listingId); // Added missing method

    List<CategoryDTO> getAllCategories(); // Added missing method

    ListingStatisticsDTO getListingStatistics(UUID userId); // Added missing method
}