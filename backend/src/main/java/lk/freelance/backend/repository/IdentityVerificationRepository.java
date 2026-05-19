package lk.freelance.backend.repository;

import lk.freelance.backend.entity.IdentityVerification;
import lk.freelance.backend.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Identity Verification Repository
 */
@Repository
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, UUID> {

    /**
     * Find verifications by user ordered by verified date descending
     */
    List<IdentityVerification> findByUser_UserIdOrderByVerifiedAtDesc(UUID userId);

    /**
     * Find latest verification for user
     */
    @Query("SELECT iv FROM IdentityVerification iv WHERE iv.user.userId = :userId ORDER BY iv.submittedAt DESC")
    Optional<IdentityVerification> findLatestByUserId(@Param("userId") UUID userId);

    /**
     * Find verifications by status
     */
    List<IdentityVerification> findByVerificationStatus(VerificationStatus status);

    /**
     * Find pending verifications
     */
    @Query("SELECT iv FROM IdentityVerification iv WHERE iv.verificationStatus = 'PENDING' ORDER BY iv.submittedAt ASC")
    List<IdentityVerification> findPendingVerifications();
}