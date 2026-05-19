package lk.freelance.backend.repository;

import lk.freelance.backend.entity.TrustScore;
import lk.freelance.backend.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trust Score Repository
 */
@Repository
public interface TrustScoreRepository extends JpaRepository<TrustScore, UUID> {

    /**
     * Find trust score by user ID
     */
    Optional<TrustScore> findByUser_UserId(UUID userId);

    /**
     * Find users with trust score above threshold
     */
    @Query("SELECT ts FROM TrustScore ts WHERE ts.overallScore >= :minScore ORDER BY ts.overallScore DESC")
    List<TrustScore> findUsersWithMinimumScore(@Param("minScore") BigDecimal minScore);

    /**
     * Get top ranked users
     */
    @Query("SELECT ts FROM TrustScore ts WHERE ts.user.role = :role ORDER BY ts.overallScore DESC")
    Page<TrustScore> findTopRankedUsersByRole(@Param("role") UserRole role, Pageable pageable);
}