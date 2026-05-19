package lk.freelance.backend.repository;

import lk.freelance.backend.entity.User;
import lk.freelance.backend.enums.UserRole;
import lk.freelance.backend.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository
 * Handles user authentication and account management
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (case-insensitive)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email exists (case-insensitive)
     */
    boolean existsByEmail(String email);

    /**
     * Find active users by role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE'")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Count users by role
     */
    long countByRole(UserRole role);

    /**
     * Find user by ID with profile eagerly loaded (avoids LazyInitializationException)
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userProfile WHERE u.userId = :userId")
    Optional<User> findByIdWithProfile(@Param("userId") UUID userId);
}