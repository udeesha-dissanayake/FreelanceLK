package lk.freelance.backend.repository;

import lk.freelance.backend.entity.ListingSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ListingSkillRepository extends JpaRepository<ListingSkill, UUID> {
    // FIX: Add this delete method to resolve the error
    void deleteAllByListing_ListingId(UUID listingId);
}