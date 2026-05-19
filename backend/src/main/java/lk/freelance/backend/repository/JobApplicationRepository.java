package lk.freelance.backend.repository;

import lk.freelance.backend.entity.JobApplication;
import lk.freelance.backend.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    boolean existsByListing_ListingIdAndFreelancer_UserId(UUID listingId, UUID freelancerId);

    Optional<JobApplication> findByListing_ListingIdAndFreelancer_UserId(UUID listingId, UUID freelancerId);

    /**
     * Single application with freelancer + profile + listing eagerly loaded.
     * Avoids LazyInitializationException in the mapper.
     */
    @Query("""
        SELECT a FROM JobApplication a
        LEFT JOIN FETCH a.freelancer f
        LEFT JOIN FETCH f.userProfile
        LEFT JOIN FETCH a.listing
        WHERE a.applicationId = :id
    """)
    Optional<JobApplication> findByIdWithDetails(@Param("id") UUID id);

    /**
     * All applications for a listing, with freelancer profiles loaded.
     */
    @Query("""
        SELECT a FROM JobApplication a
        LEFT JOIN FETCH a.freelancer f
        LEFT JOIN FETCH f.userProfile
        LEFT JOIN FETCH a.listing
        WHERE a.listing.listingId = :listingId
        ORDER BY a.createdAt DESC
    """)
    List<JobApplication> findByListingIdWithFreelancerProfile(@Param("listingId") UUID listingId);

    /**
     * All applications by a freelancer, with listing details loaded.
     */
    @Query("""
        SELECT a FROM JobApplication a
        LEFT JOIN FETCH a.freelancer f
        LEFT JOIN FETCH f.userProfile
        LEFT JOIN FETCH a.listing
        WHERE f.userId = :freelancerId
        ORDER BY a.createdAt DESC
    """)
    List<JobApplication> findByFreelancerIdWithDetails(@Param("freelancerId") UUID freelancerId);

    /**
     * Pending applications for a listing (used when auto-rejecting after accept).
     */
    @Query("SELECT a FROM JobApplication a WHERE a.listing.listingId = :listingId AND a.status = :status")
    List<JobApplication> findByListingIdAndStatus(
            @Param("listingId") UUID listingId,
            @Param("status") ApplicationStatus status
    );

    long countByListing_ListingId(UUID listingId);
}
