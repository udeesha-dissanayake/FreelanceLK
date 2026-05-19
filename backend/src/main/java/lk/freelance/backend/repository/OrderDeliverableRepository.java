package lk.freelance.backend.repository;

import lk.freelance.backend.entity.OrderDeliverable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderDeliverableRepository extends JpaRepository<OrderDeliverable, UUID> {
    // FIX: Add this method to clear the "Cannot resolve method" error
    List<OrderDeliverable> findByOrder_OrderId(UUID orderId);
}