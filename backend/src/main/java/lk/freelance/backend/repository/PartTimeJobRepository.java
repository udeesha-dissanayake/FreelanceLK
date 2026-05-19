package lk.freelance.backend.repository;

import lk.freelance.backend.entity.PartTimeJob;
import lk.freelance.backend.enums.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Part-Time Job Repository
 */
@Repository
public interface PartTimeJobRepository extends JpaRepository<PartTimeJob, UUID> {

    /**
     * Find job by listing ID (for hybrid logic)
     */
    Optional<PartTimeJob> findByListing_ListingId(UUID listingId);

    /**
     * Find jobs by employment type
     */
    @Query("SELECT j FROM PartTimeJob j WHERE j.employmentType = :type AND j.listing.status = 'ACTIVE'")
    Page<PartTimeJob> findByEmploymentType(@Param("type") EmploymentType type, Pageable pageable);

    /**
     * Find remote jobs
     */
    @Query("SELECT j FROM PartTimeJob j WHERE j.isRemote = true AND j.listing.status = 'ACTIVE'")
    Page<PartTimeJob> findRemoteJobs(Pageable pageable);

    /**
     * Find jobs by location
     */
    @Query("SELECT j FROM PartTimeJob j WHERE j.location LIKE %:location% AND j.listing.status = 'ACTIVE'")
    Page<PartTimeJob> findByLocation(@Param("location") String location, Pageable pageable);

    /**
     * Find jobs by user
     */
    @Query("SELECT j FROM PartTimeJob j WHERE j.listing.user.userId = :userId")
    List<PartTimeJob> findByUserId(@Param("userId") UUID userId);
}