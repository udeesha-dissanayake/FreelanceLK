package lk.freelance.backend.service;

import lk.freelance.backend.dto.AuthResponse;
import lk.freelance.backend.dto.LoginRequest;
import lk.freelance.backend.dto.RegistrationRequest;

public interface AuthService {
    // Basic Auth
    AuthResponse register(RegistrationRequest request);
    AuthResponse login(LoginRequest request);

    // Token Management
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);

    // Account Management
    void verifyEmail(String token);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void resendVerificationEmail(String email);
    boolean isEmailAvailable(String email);
}