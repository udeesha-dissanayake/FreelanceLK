package lk.freelance.backend.service;

import lk.freelance.backend.dto.CreateListingGigRequest;
import lk.freelance.backend.dto.CreateListingJobRequest;
import lk.freelance.backend.dto.ListingResponseDTO;
import lk.freelance.backend.dto.PagedResponse;
import lk.freelance.backend.dto.UpdateListingRequest;

import java.math.BigDecimal;
import java.util.UUID;

public interface ListingsApiService {

    ListingResponseDTO createGig(UUID userId, CreateListingGigRequest request);

    ListingResponseDTO createJob(UUID userId, CreateListingJobRequest request);

    PagedResponse<ListingResponseDTO> getListings(
            String type,
            String category,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer page,
            Integer size
    );

    ListingResponseDTO getListingById(UUID listingId);

    ListingResponseDTO updateListing(UUID userId, UUID listingId, UpdateListingRequest request);

    void deleteListing(UUID userId, UUID listingId);
}
