package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Listing;
import lk.freelance.backend.enums.ListingStatus;
import lk.freelance.backend.enums.ListingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

    @Query("""
            SELECT l FROM Listing l
            WHERE (:type IS NULL OR l.listingType = :type)
            AND (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(l.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    List<Listing> findByTypeAndKeyword(
            @Param("type") ListingType type,
            @Param("keyword") String keyword
    );

    /**
     * PostgreSQL Full-Text Search
     * Uses tsvector and tsquery for fast searching across title and description
     */
    @Query(value = "SELECT * FROM listings l WHERE " +
            "(:type IS NULL OR l.listing_type = :type) " +
            "AND l.status = :status " +
            "AND (to_tsvector('english', l.title || ' ' || l.description) @@ plainto_tsquery('english', :query))",
            nativeQuery = true)
    Page<Listing> fullTextSearch(
            @Param("query") String query,
            @Param("type") String type,
            @Param("status") String status,
            Pageable pageable
    );
}
