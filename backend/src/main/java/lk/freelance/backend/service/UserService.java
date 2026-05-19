package lk.freelance.backend.service;

import lk.freelance.backend.dto.*;
import java.util.List;
import java.util.UUID;

public interface UserService {
    // Profile Management
    UserProfileDTO getUserProfile(UUID userId);
    UserProfileDTO updateUserProfile(UUID userId, UserUpdateRequest request);
    void changePassword(UUID userId, String currentPassword, String newPassword);

    // Skills Management
    List<SkillDTO> getUserSkills(UUID userId);
    SkillDTO addSkillToUser(UUID userId, AddSkillRequest request);
    void removeSkillFromUser(UUID userId, UUID skillId);

    // Verification
    VerificationDTO submitVerification(UUID userId, SubmitVerificationRequest request);
    VerificationDTO getVerificationStatus(UUID userId); // Added missing method

    // Trust & Stats
    TrustScoreDTO getTrustScore(UUID userId); // Added missing method
    UserStatisticsDTO getUserStatistics(UUID userId); // Added missing method

    // Financial (Wallet & Transactions)
    WalletDTO getWallet(UUID userId);

    // Fixed: Now returns PagedResponse to match Controller logic
    PagedResponse<TransactionDTO> getTransactionHistory(UUID userId, Integer page, Integer size);

    WithdrawalDTO requestWithdrawal(UUID userId, WithdrawalRequest request);
    List<WithdrawalDTO> getWithdrawalHistory(UUID userId); // Added missing method

    // Account Lifecycle
    void deactivateAccount(UUID userId); // Added missing method
}