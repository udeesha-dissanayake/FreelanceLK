package lk.freelance.backend.repository;

import lk.freelance.backend.entity.TrustScoreWeight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trust Score Weight Repository
 */
@Repository
public interface TrustScoreWeightRepository extends JpaRepository<TrustScoreWeight, UUID> {

    /**
     * Find weight by name and active status
     */
    Optional<TrustScoreWeight> findByWeightNameAndIsActive(String weightName, Boolean isActive);

    /**
     * Find all active weights
     */
    List<TrustScoreWeight> findByIsActive(Boolean isActive);
}