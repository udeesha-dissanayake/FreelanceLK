package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.WalletDTO;
import lk.freelance.backend.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletDTO toDTO(Wallet wallet) {
        if (wallet == null) return null;

        return WalletDTO.builder()
                .walletId(wallet.getWalletId())
                // Ensure the User entity and userId are accessible
                .userId(wallet.getUser() != null ? wallet.getUser().getUserId() : null)
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}