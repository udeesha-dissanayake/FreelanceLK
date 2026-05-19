package lk.freelance.backend.repository;

import lk.freelance.backend.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Profile Repository
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Find profile by user ID
     */
    Optional<UserProfile> findByUser_UserId(UUID userId);

    /**
     * Check if profile exists for user
     */
    boolean existsByUser_UserId(UUID userId);

    /**
     * Find profiles by location (for local search)
     */
    List<UserProfile> findByLocationContainingIgnoreCase(String location);
}