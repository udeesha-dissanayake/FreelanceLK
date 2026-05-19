package lk.freelance.backend.repository;

import lk.freelance.backend.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * User Session Repository
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /**
     * Find active sessions by user ID
     */
    List<UserSession> findByUser_UserIdAndLogoutAtIsNull(UUID userId);

    /**
     * Count active sessions for user
     */
    long countByUser_UserIdAndLogoutAtIsNull(UUID userId);
}