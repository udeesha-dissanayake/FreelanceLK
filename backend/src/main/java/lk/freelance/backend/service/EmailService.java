package lk.freelance.backend.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
    // Add this one since it was also used in your AuthServiceImpl logic
    void resendVerificationEmail(String email);
}