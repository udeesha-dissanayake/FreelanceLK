package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.entity.*;
import lk.freelance.backend.enums.*;
import lk.freelance.backend.exception.*;
import lk.freelance.backend.repository.*;
import lk.freelance.backend.security.JwtTokenProvider;
import lk.freelance.backend.service.AuthService;
import lk.freelance.backend.service.EmailService;
import lk.freelance.backend.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Authentication Service Implementation
 * Handles all authentication and authorization operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final WalletRepository walletRepository;
    private final TrustScoreRepository trustScoreRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserMapper userMapper;

    /**
     * Register a new user account
     */
    @Override
    @Transactional
    public AuthResponse register(RegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        // Validate role
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be FREELANCER or CLIENT");
        }

        if (role == UserRole.ADMIN || role == UserRole.MODERATOR) {
            throw new BadRequestException("Cannot register as ADMIN or MODERATOR");
        }

        // Create user entity
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        // Create user profile
        UserProfile profile = UserProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .displayName(request.getFirstName() + " " + request.getLastName())
                .location(request.getLocation())
                .bio(request.getBio())
                .timezone("Asia/Colombo")
                .language("en")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userProfileRepository.save(profile);

        // Initialize wallet
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .pendingBalance(BigDecimal.ZERO)
                .currency("LKR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        walletRepository.save(wallet);

        // Initialize trust score
        TrustScore trustScore = TrustScore.builder()
                .user(user)
                .overallScore(BigDecimal.ZERO)
                .reviewScore(BigDecimal.ZERO)
                .verificationScore(BigDecimal.ZERO)
                .transactionConsistencyScore(BigDecimal.ZERO)
                .completionRate(BigDecimal.ZERO)
                .responseTimeScore(BigDecimal.valueOf(75.0)) // Default
                .totalReviews(0)
                .totalCompletedOrders(0)
                .totalCancelledOrders(0)
                .lastCalculatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        trustScoreRepository.save(trustScore);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        // Store token in database (implementation depends on your token storage strategy)

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // Save refresh token
        saveRefreshToken(user.getUserId(), refreshToken);

        // Create user session
        createUserSession(user.getUserId(), "Registration");

        log.info("User registered successfully: {}", user.getEmail());

        // Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .user(userMapper.toUserProfileDTO(user, profile, trustScore))
                .build();
    }

    /**
     * Login user with email and password
     */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Check account status
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountSuspendedException();
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("Account is inactive. Please contact support.");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // Revoke old refresh tokens and save new one
        revokeUserRefreshTokens(user.getUserId());
        saveRefreshToken(user.getUserId(), refreshToken);

        // Create user session
        createUserSession(user.getUserId(), "Login");

        // Get user profile and trust score
        UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", user.getUserId()));

        TrustScore trustScore = trustScoreRepository.findByUser_UserId(user.getUserId())
                .orElse(null);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .user(userMapper.toUserProfileDTO(user, profile, trustScore))
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        log.info("Refreshing access token");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshTokenString)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        // Get user ID from token
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshTokenString);

        // Verify token exists in database and not revoked
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashAndIsRevoked(
                        hashToken(refreshTokenString),
                        false
                )
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found or revoked"));

        // Check if token is expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail()
        );

        // Optionally rotate refresh token
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // Revoke old refresh token
        refreshToken.setIsRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        // Save new refresh token
        saveRefreshToken(user.getUserId(), newRefreshToken);

        // Get user profile and trust score
        UserProfile profile = userProfileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "userId", user.getUserId()));

        TrustScore trustScore = trustScoreRepository.findByUser_UserId(user.getUserId())
                .orElse(null);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationInSeconds())
                .user(userMapper.toUserProfileDTO(user, profile, trustScore))
                .build();
    }

    /**
     * Logout user and revoke refresh token
     */
    @Override
    @Transactional
    public void logout(String refreshTokenString) {
        log.info("Logging out user");

        // Find and revoke refresh token
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashAndIsRevoked(
                        hashToken(refreshTokenString),
                        false
                )
                .orElse(null);

        if (refreshToken != null) {
            refreshToken.setIsRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);

            log.info("Refresh token revoked for user: {}", refreshToken.getUser().getUserId());
        }

        // Clear security context
        SecurityContextHolder.clearContext();
    }

    /**
     * Verify email with verification token
     */
    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        // Validate token format
        UUID tokenUuid;
        try {
            tokenUuid = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid verification token format");
        }

        // This is a placeholder - implement proper token verification
        throw new BadRequestException("Email verification not fully implemented");
    }

    /**
     * Initiate password reset
     */
    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        // Find user (don't reveal if email exists or not for security)
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElse(null);

        if (user != null) {
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();

            // Store reset token (implementation depends on your strategy)

            // Send password reset email
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

            log.info("Password reset email sent to: {}", email);
        }

        // Always return success to prevent email enumeration
    }

    /**
     * Reset password with reset token
     */
    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");

        // This is a placeholder implementation
        throw new BadRequestException("Password reset not fully implemented");
    }

    /**
     * Resend verification email
     */
    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        log.info("Verification email resent to: {}", email);
    }

    /**
     * Check if email is available
     */
    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email.toLowerCase());
    }

    // ================================================================
    // PRIVATE HELPER METHODS
    // ================================================================

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(UUID userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(token))
                .expiresAt(LocalDateTime.now().plusDays(7)) // 7 days
                .isRevoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoke all refresh tokens for a user
     */
    private void revokeUserRefreshTokens(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUser_UserIdAndIsRevoked(
                userId,
                false
        );

        tokens.forEach(token -> {
            token.setIsRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
        });

        refreshTokenRepository.saveAll(tokens);
    }

    /**
     * Create user session record
     */
    private void createUserSession(UUID userId, String loginMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserSession session = UserSession.builder()
                .user(user)
                .ipAddress("0.0.0.0") // Placeholder - get from request
                .userAgent(loginMethod)
                .loginAt(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .build();

        userSessionRepository.save(session);
    }

    /**
     * Hash token for storage using SHA-256.
     * BCrypt cannot be used here because it generates a different hash every call
     * (random salt), making it impossible to look up a stored token by its hash.
     * SHA-256 is deterministic: same input always produces the same output.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}