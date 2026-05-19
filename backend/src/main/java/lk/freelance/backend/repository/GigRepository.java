package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Gig;
import lk.freelance.backend.enums.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GigRepository extends JpaRepository<Gig, UUID> {

    Optional<Gig> findByListing_ListingId(UUID listingId);

    // --- ADDED: Required for getUserGigs in Service ---
    Page<Gig> findByListing_User_UserId(UUID userId, Pageable pageable);

    // --- ADDED: Required for getUserGigs with status filter ---
    Page<Gig> findByListing_User_UserIdAndListing_Status(UUID userId, ListingStatus status, Pageable pageable);

    @Query("SELECT g FROM Gig g WHERE g.listing.status = :status")
    Page<Gig> findByListingStatus(@Param("status") ListingStatus status, Pageable pageable);

    @Query("SELECT g FROM Gig g WHERE " +
            "g.listing.status = 'ACTIVE' " +
            "AND (:category IS NULL OR g.category = :category) " +
            "AND g.basePrice >= :minPrice " +
            "AND g.basePrice <= :maxPrice " +
            "AND g.deliveryDays <= :maxDeliveryDays")
    Page<Gig> findByFilters(
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("maxDeliveryDays") Integer maxDeliveryDays,
            Pageable pageable
    );

    @Query("SELECT g FROM Gig g WHERE g.category = :category AND g.listing.status = 'ACTIVE'")
    Page<Gig> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT g FROM Gig g WHERE g.listing.user.userId = :userId")
    List<Gig> findByUserId(@Param("userId") UUID userId);

    // --- ADDED: Required for cleanup during deleteGig ---
    @Modifying
    @Transactional
    @Query("DELETE FROM GigPackage gp WHERE gp.gig.gigId = :gigId")
    void deleteAllPackagesByGigId(@Param("gigId") UUID gigId);
}