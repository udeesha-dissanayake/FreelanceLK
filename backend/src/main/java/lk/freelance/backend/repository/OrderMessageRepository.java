package lk.freelance.backend.repository;

import lk.freelance.backend.entity.OrderMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Message Repository
 */
@Repository
public interface OrderMessageRepository extends JpaRepository<OrderMessage, UUID> {

    /**
     * Find messages for an order ordered by creation date
     */
    List<OrderMessage> findByOrderOrderIdOrderByCreatedAtAsc(UUID orderId);

    /**
     * Find messages by order and read status
     */
    List<OrderMessage> findByOrderOrderIdAndIsRead(UUID orderId, Boolean isRead);

    /**
     * Count unread messages for an order by recipient
     */
    @Query("SELECT COUNT(om) FROM OrderMessage om WHERE om.order.orderId = :orderId AND om.sender.userId != :userId AND om.isRead = false")
    Long countUnreadMessagesForUser(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    /**
     * Find latest message for an order
     */
    @Query("SELECT om FROM OrderMessage om WHERE om.order.orderId = :orderId ORDER BY om.createdAt DESC")
    Optional<OrderMessage> findLatestMessageForOrder(@Param("orderId") UUID orderId);
}