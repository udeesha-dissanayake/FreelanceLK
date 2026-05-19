package lk.freelance.backend.repository;

import lk.freelance.backend.entity.ListingMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ListingMediaRepository extends JpaRepository<ListingMedia, UUID> {
    // FIX: Add this method
    void deleteAllByListing_ListingId(UUID listingId);
}