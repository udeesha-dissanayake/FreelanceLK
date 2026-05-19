package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // FIX: This method must return a List to work with .forEach()
    List<Transaction> findByOrder_OrderId(UUID orderId);

    // Keep your existing specific query if needed
    Optional<Transaction> findByOrder_OrderIdAndTransactionType(UUID orderId, String transactionType);

    // Required for UserServiceImpl logic we just wrote
    org.springframework.data.domain.Page<Transaction> findByPayer_UserIdOrPayee_UserId(
            UUID payerId, UUID payeeId, org.springframework.data.domain.Pageable pageable);
}