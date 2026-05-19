package lk.freelance.backend.repository;

import lk.freelance.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token Repository
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token hash and revoked status
     */
    Optional<RefreshToken> findByTokenHashAndIsRevoked(String tokenHash, Boolean isRevoked);

    /**
     * Find all tokens for a user by revoked status
     */
    List<RefreshToken> findByUser_UserIdAndIsRevoked(UUID userId, Boolean isRevoked);

    /**
     * Delete all tokens for a user
     */
    void deleteByUser_UserId(UUID userId);

    /**
     * Find expired tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP AND rt.isRevoked = false")
    List<RefreshToken> findExpiredTokens();
}
