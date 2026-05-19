package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.WithdrawalDTO;
import lk.freelance.backend.entity.WithdrawalRequest;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalMapper {

    public WithdrawalDTO toDTO(WithdrawalRequest withdrawal) {
        if (withdrawal == null) return null;

        return WithdrawalDTO.builder()
                .withdrawalId(withdrawal.getWithdrawalId())
                .userId(withdrawal.getUser() != null ? withdrawal.getUser().getUserId() : null)
                .amount(withdrawal.getAmount())
                .status(withdrawal.getStatus())
                // FIX: Null-safe map access
                .bankName(withdrawal.getBankAccountDetails() != null ?
                        withdrawal.getBankAccountDetails().get("bankName") : null)
                .accountNumber(withdrawal.getBankAccountDetails() != null ?
                        withdrawal.getBankAccountDetails().get("accountNumber") : null)
                .accountName(withdrawal.getBankAccountDetails() != null ?
                        withdrawal.getBankAccountDetails().get("accountName") : null)
                .requestedAt(withdrawal.getRequestedAt())
                .processedAt(withdrawal.getProcessedAt())
                .notes(withdrawal.getNotes())
                .build();
    }
}