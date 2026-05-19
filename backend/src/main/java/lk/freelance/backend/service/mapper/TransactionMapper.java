package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.TransactionDTO;
import lk.freelance.backend.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) return null;

        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                // Use the order relationship if it exists
                .orderId(transaction.getOrder() != null ? transaction.getOrder().getOrderId() : null)
                // In your entity, you track Payer or Payee
                .userId(transaction.getPayer() != null ? transaction.getPayer().getUserId() :
                        (transaction.getPayee() != null ? transaction.getPayee().getUserId() : null))
                .amount(transaction.getAmount())
                // FIX: Changed getType() to getTransactionType()
                .transactionType(transaction.getTransactionType())
                // FIX: Added null check for Status Enum
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : "PENDING")
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .build();
    }
}