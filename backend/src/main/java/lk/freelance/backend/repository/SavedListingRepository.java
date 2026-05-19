package lk.freelance.backend.repository;

import lk.freelance.backend.entity.SavedListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Saved Listing Repository
 */
@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, UUID> {

    /**
     * Find saved listings by user
     */
    List<SavedListing> findByUser_UserId(UUID userId);

    /**
     * Find saved listings by user with pagination
     */
    Page<SavedListing> findByUser_UserId(UUID userId, Pageable pageable);

    /**
     * Check if user has saved a listing
     */
    boolean existsByUser_UserIdAndListing_ListingId(UUID userId, UUID listingId);

    /**
     * Delete saved listing
     */
    void deleteByUser_UserIdAndListing_ListingId(UUID userId, UUID listingId);

    /**
     * Count saved listings for a listing
     */
    long countByListing_ListingId(UUID listingId);
}