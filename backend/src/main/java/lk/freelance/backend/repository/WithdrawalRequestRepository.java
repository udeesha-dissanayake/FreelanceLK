package lk.freelance.backend.repository;

import lk.freelance.backend.entity.WithdrawalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Withdrawal Request Repository
 */
@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, UUID> {

    /**
     * Find withdrawal requests by user
     */
    List<WithdrawalRequest> findByUser_UserId(UUID userId);

    /**
     * Find withdrawal requests by user with pagination
     */
    Page<WithdrawalRequest> findByUser_UserId(UUID userId, Pageable pageable);

    /**
     * Find withdrawal requests by status
     */
    List<WithdrawalRequest> findByStatus(String status);

    /**
     * Find pending withdrawal requests
     */
    @Query("SELECT wr FROM WithdrawalRequest wr WHERE wr.status = 'PENDING' ORDER BY wr.requestedAt ASC")
    List<WithdrawalRequest> findPendingRequests();
}