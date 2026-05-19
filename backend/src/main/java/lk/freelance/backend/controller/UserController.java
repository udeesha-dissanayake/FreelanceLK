// ================================================================
// USER CONTROLLER
// FreelanceLK.com - User Profile & Account Management
// ================================================================
// Handles user profile, skills, verification, and account operations
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-01-30
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.service.UserService;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for user profile and account management
 * Base URL: /api/v1/users
 *
 * @apiNote Most endpoints require authentication
 * @security User-specific data access controls
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UserController {

    private final UserService userService;

    /**
     * Get current user's profile
     * GET /api/v1/users/me
     *
     * @param currentUser Authenticated user from JWT
     * @return User profile with trust score and skills
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getCurrentUser(
            @CurrentUser UserPrincipal currentUser
    ) {
        UserProfileDTO profile = userService.getUserProfile(currentUser.getUserId());

        return ResponseEntity
                .ok(ApiResponse.success(profile));
    }

    /**
     * Get user profile by ID
     * GET /api/v1/users/{userId}
     *
     * @param userId User ID
     * @return User profile
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserById(
            @PathVariable UUID userId
    ) {
        UserProfileDTO profile = userService.getUserProfile(userId);

        return ResponseEntity
                .ok(ApiResponse.success(profile));
    }

    /**
     * Update current user's profile
     * PUT /api/v1/users/me
     *
     * @param currentUser Authenticated user
     * @param request Updated profile data
     * @return Updated user profile
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserProfileDTO updatedProfile = userService.updateUserProfile(
                currentUser.getUserId(),
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Profile updated successfully!",
                        updatedProfile
                ));
    }

    /**
     * Change password
     * POST /api/v1/users/me/change-password
     *
     * @param currentUser Authenticated user
     * @param request Password change request
     * @return Success message
     */
    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(
                currentUser.getUserId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        return ResponseEntity
                .ok(ApiResponse.success("Password changed successfully!"));
    }

    /**
     * Get user's skills
     * GET /api/v1/users/me/skills
     *
     * @param currentUser Authenticated user
     * @return List of user skills
     */
    @GetMapping("/me/skills")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SkillDTO>>> getMySkills(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<SkillDTO> skills = userService.getUserSkills(currentUser.getUserId());

        return ResponseEntity
                .ok(ApiResponse.success(skills));
    }

    /**
     * Add skill to user profile
     * POST /api/v1/users/me/skills
     *
     * @param currentUser Authenticated user
     * @param request Skill to add
     * @return Added skill
     */
    @PostMapping("/me/skills")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SkillDTO>> addSkill(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody AddSkillRequest request
    ) {
        SkillDTO skill = userService.addSkillToUser(currentUser.getUserId(), request);

        return ResponseEntity
                .ok(ApiResponse.success("Skill added successfully!", skill));
    }

    /**
     * Remove skill from user profile
     * DELETE /api/v1/users/me/skills/{skillId}
     *
     * @param currentUser Authenticated user
     * @param skillId Skill ID to remove
     * @return Success message
     */
    @DeleteMapping("/me/skills/{skillId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeSkill(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID skillId
    ) {
        userService.removeSkillFromUser(currentUser.getUserId(), skillId);

        return ResponseEntity
                .ok(ApiResponse.success("Skill removed successfully!"));
    }

    /**
     * Submit identity verification
     * POST /api/v1/users/me/verification
     *
     * @param currentUser Authenticated user
     * @param request Verification documents
     * @return Verification status
     */
    @PostMapping("/me/verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerificationDTO>> submitVerification(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody SubmitVerificationRequest request
    ) {
        VerificationDTO verification = userService.submitVerification(
                currentUser.getUserId(),
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Verification submitted successfully! We'll review it within 24-48 hours.",
                        verification
                ));
    }

    /**
     * Get verification status
     * GET /api/v1/users/me/verification
     *
     * @param currentUser Authenticated user
     * @return Verification status
     */
    @GetMapping("/me/verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerificationDTO>> getVerificationStatus(
            @CurrentUser UserPrincipal currentUser
    ) {
        VerificationDTO verification = userService.getVerificationStatus(
                currentUser.getUserId()
        );

        return ResponseEntity
                .ok(ApiResponse.success(verification));
    }

    /**
     * Get user's trust score
     * GET /api/v1/users/{userId}/trust-score
     *
     * @param userId User ID
     * @return Trust score details
     */
    @GetMapping("/{userId}/trust-score")
    public ResponseEntity<ApiResponse<TrustScoreDTO>> getTrustScore(
            @PathVariable UUID userId
    ) {
        TrustScoreDTO trustScore = userService.getTrustScore(userId);

        return ResponseEntity
                .ok(ApiResponse.success(trustScore));
    }

    /**
     * Get user's wallet
     * GET /api/v1/users/me/wallet
     *
     * @param currentUser Authenticated user
     * @return Wallet balance information
     */
    @GetMapping("/me/wallet")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WalletDTO>> getWallet(
            @CurrentUser UserPrincipal currentUser
    ) {
        WalletDTO wallet = userService.getWallet(currentUser.getUserId());

        return ResponseEntity
                .ok(ApiResponse.success(wallet));
    }

    /**
     * Get transaction history
     * GET /api/v1/users/me/transactions
     *
     * @param currentUser Authenticated user
     * @param page Page number
     * @param size Page size
     * @return Paginated transaction history
     */
    @GetMapping("/me/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionDTO>>> getTransactions(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<TransactionDTO> transactions = userService.getTransactionHistory(
                currentUser.getUserId(),
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(transactions));
    }

    /**
     * Request withdrawal
     * POST /api/v1/users/me/withdrawals
     *
     * @param currentUser Authenticated user
     * @param request Withdrawal details
     * @return Withdrawal request details
     */
    @PostMapping("/me/withdrawals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WithdrawalDTO>> requestWithdrawal(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody WithdrawalRequest request
    ) {
        WithdrawalDTO withdrawal = userService.requestWithdrawal(
                currentUser.getUserId(),
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Withdrawal request submitted successfully! We'll process it within 3-5 business days.",
                        withdrawal
                ));
    }

    /**
     * Get withdrawal history
     * GET /api/v1/users/me/withdrawals
     *
     * @param currentUser Authenticated user
     * @return List of withdrawal requests
     */
    @GetMapping("/me/withdrawals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<WithdrawalDTO>>> getWithdrawals(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<WithdrawalDTO> withdrawals = userService.getWithdrawalHistory(
                currentUser.getUserId()
        );

        return ResponseEntity
                .ok(ApiResponse.success(withdrawals));
    }

    /**
     * Get user statistics (for profile page)
     * GET /api/v1/users/{userId}/stats
     *
     * @param userId User ID
     * @return User statistics
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<ApiResponse<UserStatisticsDTO>> getUserStatistics(
            @PathVariable UUID userId
    ) {
        UserStatisticsDTO stats = userService.getUserStatistics(userId);

        return ResponseEntity
                .ok(ApiResponse.success(stats));
    }

    /**
     * Deactivate account
     * POST /api/v1/users/me/deactivate
     *
     * @param currentUser Authenticated user
     * @return Success message
     */
    @PostMapping("/me/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deactivateAccount(
            @CurrentUser UserPrincipal currentUser
    ) {
        userService.deactivateAccount(currentUser.getUserId());

        return ResponseEntity
                .ok(ApiResponse.success("Account deactivated successfully!"));
    }
}