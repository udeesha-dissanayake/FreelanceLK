package lk.freelance.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lk.freelance.backend.dto.ApiResponse;
import lk.freelance.backend.dto.CreateListingGigRequest;
import lk.freelance.backend.dto.CreateListingJobRequest;
import lk.freelance.backend.dto.ListingResponseDTO;
import lk.freelance.backend.dto.PagedResponse;
import lk.freelance.backend.dto.UpdateListingRequest;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lk.freelance.backend.service.ListingsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/listings")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@Tag(name = "Listings API", description = "Unified endpoints for gigs and jobs")
public class ListingsApiController {

    private final ListingsApiService listingsApiService;

    @PostMapping("/gigs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a gig listing")
    public ResponseEntity<ApiResponse<ListingResponseDTO>> createGig(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateListingGigRequest request
    ) {
        ListingResponseDTO created = listingsApiService.createGig(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gig listing created", created));
    }

    @PostMapping("/jobs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a job listing")
    public ResponseEntity<ApiResponse<ListingResponseDTO>> createJob(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateListingJobRequest request
    ) {
        ListingResponseDTO created = listingsApiService.createJob(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job listing created", created));
    }

    @GetMapping
    @Operation(summary = "List listings with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<ListingResponseDTO>>> getListings(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DecimalMin(value = "0.0", inclusive = true) BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin(value = "0.0", inclusive = true) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size
    ) {
        PagedResponse<ListingResponseDTO> listings = listingsApiService.getListings(
                type, category, keyword, minPrice, maxPrice, page, size
        );
        return ResponseEntity.ok(ApiResponse.success(listings));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing by id")
    public ResponseEntity<ApiResponse<ListingResponseDTO>> getListingById(
            @PathVariable("id") UUID listingId
    ) {
        ListingResponseDTO listing = listingsApiService.getListingById(listingId);
        return ResponseEntity.ok(ApiResponse.success(listing));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update listing by id (owner only)")
    public ResponseEntity<ApiResponse<ListingResponseDTO>> updateListing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") UUID listingId,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        ListingResponseDTO updated = listingsApiService.updateListing(currentUser.getUserId(), listingId, request);
        return ResponseEntity.ok(ApiResponse.success("Listing updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete listing by id (owner only)")
    public ResponseEntity<ApiResponse<Void>> deleteListing(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") UUID listingId
    ) {
        listingsApiService.deleteListing(currentUser.getUserId(), listingId);
        return ResponseEntity.ok(ApiResponse.success("Listing deleted"));
    }
}
