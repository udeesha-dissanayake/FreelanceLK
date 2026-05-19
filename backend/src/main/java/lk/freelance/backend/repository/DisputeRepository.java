package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Dispute Repository
 */
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    /**
     * Find disputes by order
     */
    Optional<Dispute> findByOrder_OrderId(UUID orderId);

    /**
     * Find disputes by raised by user
     */
    List<Dispute> findByRaisedBy_UserId(UUID userId);

    /**
     * Find disputes by status
     */
    List<Dispute> findByStatus(String status);

    /**
     * Find open disputes
     */
    @Query("SELECT d FROM Dispute d WHERE d.status IN ('OPEN', 'IN_REVIEW')")
    List<Dispute> findOpenDisputes();

    /**
     * Count disputes by user
     */
    Long countByRaisedBy_UserId(UUID userId);
}