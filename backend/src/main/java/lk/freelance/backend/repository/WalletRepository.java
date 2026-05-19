package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Wallet Repository - CRITICAL for Escrow Logic
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Find wallet by user ID (CRITICAL for escrow operations)
     */
    Optional<Wallet> findByUser_UserId(UUID userId);

    /**
     * Check if wallet exists for user
     */
    boolean existsByUser_UserId(UUID userId);

    /**
     * Find wallets with balance greater than amount
     */
    @Query("SELECT w FROM Wallet w WHERE w.balance >= :minBalance")
    List<Wallet> findWalletsWithMinimumBalance(@Param("minBalance") BigDecimal minBalance);
}