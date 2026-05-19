// ================================================================
// AUTHENTICATION CONTROLLER
// FreelanceLK.com - Auth REST API
// ================================================================
// Handles registration, login, token refresh, and logout
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-01-30
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
/**
 * REST Controller for authentication operations
 * Base URL: /api/v1/auth
 *
 * @apiNote All endpoints require proper validation and return standardized ApiResponse
 * @security Public endpoints (except logout/refresh)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account
     * POST /api/v1/auth/register
     *
     * @param request Registration details
     * @return Auth response with JWT tokens and user profile
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful! Please verify your email.",
                        authResponse
                ));
    }

    /**
     * Login with email and password
     * POST /api/v1/auth/login
     *
     * @param request Login credentials
     * @return Auth response with JWT tokens and user profile
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Login successful!",
                        authResponse
                ));
    }

    /**
     * Refresh access token using refresh token
     * POST /api/v1/auth/refresh
     *
     * @param request Refresh token
     * @return New auth response with refreshed tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Token refreshed successfully!",
                        authResponse
                ));
    }

    /**
     * Logout and invalidate refresh token
     * POST /api/v1/auth/logout
     *
     * @param request Refresh token to revoke
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request.getRefreshToken());

        return ResponseEntity
                .ok(ApiResponse.success("Logout successful!"));
    }

    /**
     * Verify email with verification token
     * GET /api/v1/auth/verify-email?token={token}
     *
     * @param token Email verification token
     * @return Success message
     */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token
    ) {
        authService.verifyEmail(token);

        return ResponseEntity
                .ok(ApiResponse.success("Email verified successfully!"));
    }

    /**
     * Request password reset
     * POST /api/v1/auth/forgot-password
     *
     * @param email User's email address
     * @return Success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestParam String email
    ) {
        authService.initiatePasswordReset(email);

        return ResponseEntity
                .ok(ApiResponse.success(
                        "If an account exists with that email, a password reset link has been sent."
                ));
    }

    /**
     * Reset password with reset token
     * POST /api/v1/auth/reset-password
     *
     * @param token Reset token
     * @param newPassword New password
     * @return Success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        authService.resetPassword(token, newPassword);

        return ResponseEntity
                .ok(ApiResponse.success("Password reset successfully!"));
    }

    /**
     * Resend email verification
     * POST /api/v1/auth/resend-verification
     *
     * @param email User's email address
     * @return Success message
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestParam String email
    ) {
        authService.resendVerificationEmail(email);

        return ResponseEntity
                .ok(ApiResponse.success("Verification email sent!"));
    }

    /**
     * Check if email is available
     * GET /api/v1/auth/check-email?email={email}
     *
     * @param email Email to check
     * @return Boolean indicating availability
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @RequestParam String email
    ) {
        boolean isAvailable = authService.isEmailAvailable(email);

        return ResponseEntity
                .ok(ApiResponse.success(isAvailable));
    }
}