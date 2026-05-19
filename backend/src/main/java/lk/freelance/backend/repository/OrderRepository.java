package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Order;
import lk.freelance.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    // --- Paging Methods for Dashboard ---
    Page<Order> findByBuyer_UserId(UUID userId, Pageable pageable);

    Page<Order> findBySeller_UserId(UUID userId, Pageable pageable);

    Page<Order> findByBuyer_UserIdOrSeller_UserId(UUID buyerId, UUID sellerId, Pageable pageable);

    // --- Trust Score & Stats Support Methods ---

    // Required for calculating Response Time Score
    List<Order> findBySeller_UserId(UUID userId);

    // Required for calculating Completion Rate (Total Orders)
    Long countBySeller_UserId(UUID userId);

    // Required for calculating Success/Failure ratios
    Long countBySeller_UserIdAndStatus(UUID userId, OrderStatus status);

    // Dashboard: count orders by multiple statuses (e.g. IN_PROGRESS + ACCEPTED)
    Long countBySeller_UserIdAndStatusIn(UUID userId, java.util.List<OrderStatus> statuses);

    // Dashboard: sum of completed order amounts = total all-time earnings
    @org.springframework.data.jpa.repository.Query(
        "SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.seller.userId = :userId AND o.status = :status"
    )
    java.math.BigDecimal sumAmountBySeller_UserIdAndStatus(
        @org.springframework.data.repository.query.Param("userId") UUID userId,
        @org.springframework.data.repository.query.Param("status") OrderStatus status
    );
}