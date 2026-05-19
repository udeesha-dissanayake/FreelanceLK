// ================================================================
// ADMIN CONTROLLER
// FreelanceLK.com - Admin Dashboard & Platform Management
// ================================================================
// Handles identity verification approvals, user management,
// dispute resolution, and platform statistics
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-01-30
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.ApiResponse;
import lk.freelance.backend.dto.DisputeDTO;
import lk.freelance.backend.dto.UserDetailDTO;
import lk.freelance.backend.dto.VerificationDTO;
import lk.freelance.backend.entity.Dispute;
import lk.freelance.backend.entity.IdentityVerification;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.enums.UserStatus;
import lk.freelance.backend.enums.VerificationStatus;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.repository.DisputeRepository;
import lk.freelance.backend.repository.IdentityVerificationRepository;
import lk.freelance.backend.repository.UserRepository;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lk.freelance.backend.service.mapper.VerificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for admin operations
 * Base URL: /api/v1/admin
 *
 * @apiNote All endpoints restricted to ADMIN or MODERATOR roles only
 * @security ROLE_ADMIN or ROLE_MODERATOR required
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AdminController {

    private final IdentityVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final DisputeRepository disputeRepository;
    private final VerificationMapper verificationMapper;

    // ----------------------------------------------------------------
    // REQUEST BODY — Verification Decision
    // ----------------------------------------------------------------
    record VerificationDecisionRequest(
            @NotBlank String decision,  // "APPROVE" or "REJECT"
            String notes                // optional rejection reason
    ) {}

    // ----------------------------------------------------------------
    // REQUEST BODY — Dispute Resolution
    // ----------------------------------------------------------------
    record DisputeResolutionRequest(
            @NotBlank String resolution,
            @NotBlank String status     // "RESOLVED" or "CLOSED"
    ) {}

    // ================================================================
    // IDENTITY VERIFICATION MANAGEMENT
    // ================================================================

    /**
     * GET /api/v1/admin/verifications/pending
     * Get all pending identity verification requests
     */
    @GetMapping("/verifications/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<List<VerificationDTO>>> getPendingVerifications() {
        List<VerificationDTO> pending = verificationRepository.findPendingVerifications()
                .stream()
                .map(verificationMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                "Found " + pending.size() + " pending verification(s).",
                pending
        ));
    }

    /**
     * GET /api/v1/admin/verifications
     * Get all verification requests (optionally filter by status)
     */
    @GetMapping("/verifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<List<VerificationDTO>>> getAllVerifications(
            @RequestParam(required = false) String status
    ) {
        List<IdentityVerification> verifications;

        if (status != null) {
            try {
                VerificationStatus vs = VerificationStatus.valueOf(status.toUpperCase());
                verifications = verificationRepository.findByVerificationStatus(vs);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid status. Use: PENDING, VERIFIED, REJECTED, UNVERIFIED"));
            }
        } else {
            verifications = verificationRepository.findAll();
        }

        List<VerificationDTO> result = verifications.stream()
                .map(verificationMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/v1/admin/verifications/{verificationId}
     * Get a single verification request
     */
    @GetMapping("/verifications/{verificationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<VerificationDTO>> getVerificationById(
            @PathVariable UUID verificationId
    ) {
        IdentityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found"));

        return ResponseEntity.ok(ApiResponse.success(verificationMapper.toDTO(verification)));
    }

    /**
     * POST /api/v1/admin/verifications/{verificationId}/decide
     * Approve or reject an identity verification
     */
    @PostMapping("/verifications/{verificationId}/decide")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public ResponseEntity<ApiResponse<VerificationDTO>> decideVerification(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID verificationId,
            @RequestBody VerificationDecisionRequest request
    ) {
        IdentityVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request not found"));

        if (verification.getVerificationStatus() != VerificationStatus.PENDING) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("This verification has already been processed."));
        }

        boolean isApproved = "APPROVE".equalsIgnoreCase(request.decision());

        verification.setVerificationStatus(isApproved ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setVerifiedBy(currentUser.getUserId().toString());
        verification.setNotes(request.notes());
        verification.setUpdatedAt(LocalDateTime.now());

        verificationRepository.save(verification);

        String message = isApproved
                ? "Verification approved successfully."
                : "Verification rejected.";

        return ResponseEntity.ok(ApiResponse.success(message, verificationMapper.toDTO(verification)));
    }

    // ================================================================
    // USER MANAGEMENT
    // ================================================================

    /**
     * GET /api/v1/admin/users
     * Get all users (optionally filter by status or role)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDetailDTO>>> getAllUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role
    ) {
        List<User> users = userRepository.findAll();

        // Filter by status if provided
        if (status != null) {
            try {
                UserStatus us = UserStatus.valueOf(status.toUpperCase());
                users = users.stream()
                        .filter(u -> u.getStatus() == us)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid status. Use: ACTIVE, SUSPENDED, INACTIVE, PENDING_VERIFICATION"));
            }
        }

        // Filter by role if provided
        if (role != null) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && u.getRole().name().equalsIgnoreCase(role))
                    .collect(Collectors.toList());
        }

        List<UserDetailDTO> result = users.stream()
                .map(u -> UserDetailDTO.builder()
                        .userId(u.getUserId())
                        .email(u.getEmail())
                        .displayName(u.getUserProfile() != null ? u.getUserProfile().getDisplayName() : "N/A")
                        .firstName(u.getUserProfile() != null ? u.getUserProfile().getFirstName() : null)
                        .lastName(u.getUserProfile() != null ? u.getUserProfile().getLastName() : null)
                        .avatarUrl(u.getUserProfile() != null ? u.getUserProfile().getAvatarUrl() : null)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/v1/admin/users/{userId}/suspend
     * Suspend a user account
     */
    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @PathVariable UUID userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User is already suspended."));
        }

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User account has been suspended."));
    }

    /**
     * POST /api/v1/admin/users/{userId}/activate
     * Reactivate a suspended user account
     */
    @PostMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> activateUser(
            @PathVariable UUID userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User account has been activated."));
    }

    // ================================================================
    // DISPUTE MANAGEMENT
    // ================================================================

    /**
     * GET /api/v1/admin/disputes
     * Get all open disputes
     */
    @GetMapping("/disputes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<List<DisputeDTO>>> getOpenDisputes() {
        List<DisputeDTO> disputes = disputeRepository.findOpenDisputes()
                .stream()
                .map(this::toDisputeDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                "Found " + disputes.size() + " open dispute(s).",
                disputes
        ));
    }

    /**
     * GET /api/v1/admin/disputes/{disputeId}
     * Get a single dispute
     */
    @GetMapping("/disputes/{disputeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<DisputeDTO>> getDisputeById(
            @PathVariable UUID disputeId
    ) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        return ResponseEntity.ok(ApiResponse.success(toDisputeDTO(dispute)));
    }

    /**
     * POST /api/v1/admin/disputes/{disputeId}/resolve
     * Resolve a dispute
     */
    @PostMapping("/disputes/{disputeId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public ResponseEntity<ApiResponse<DisputeDTO>> resolveDispute(
            @PathVariable UUID disputeId,
            @RequestBody DisputeResolutionRequest request
    ) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));

        dispute.setResolution(request.resolution());
        dispute.setStatus(request.status());
        dispute.setResolvedAt(LocalDateTime.now());

        disputeRepository.save(dispute);

        return ResponseEntity.ok(ApiResponse.success("Dispute has been resolved.", toDisputeDTO(dispute)));
    }

    // ================================================================
    // PLATFORM STATS
    // ================================================================

    /**
     * GET /api/v1/admin/stats
     * Basic platform statistics for admin dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<PlatformStats>> getPlatformStats() {
        long totalUsers       = userRepository.count();
        long pendingVerifs    = verificationRepository.findPendingVerifications().size();
        long openDisputes     = disputeRepository.findOpenDisputes().size();
        long suspendedUsers   = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.SUSPENDED).count();

        PlatformStats stats = new PlatformStats(
                totalUsers,
                pendingVerifs,
                openDisputes,
                suspendedUsers
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private DisputeDTO toDisputeDTO(Dispute d) {
        return DisputeDTO.builder()
                .disputeId(d.getDisputeId())
                .orderId(d.getOrder() != null ? d.getOrder().getOrderId() : null)
                .orderNumber(d.getOrder() != null ? d.getOrder().getOrderNumber() : null)
                .raisedBy(d.getRaisedBy() != null ? d.getRaisedBy().getUserId() : null)
                .raisedByName(d.getRaisedBy() != null && d.getRaisedBy().getUserProfile() != null
                        ? d.getRaisedBy().getUserProfile().getDisplayName()
                        : "Unknown")
                .reason(d.getReason())
                .description(d.getDescription())
                .status(d.getStatus())
                .resolution(d.getResolution())
                .createdAt(d.getCreatedAt())
                .resolvedAt(d.getResolvedAt())
                .build();
    }

    record PlatformStats(
            long totalUsers,
            long pendingVerifications,
            long openDisputes,
            long suspendedUsers
    ) {}
}
