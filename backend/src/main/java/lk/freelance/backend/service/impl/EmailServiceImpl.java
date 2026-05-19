package lk.freelance.backend.service.impl;

import lk.freelance.backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendVerificationEmail(String to, String token) {
        // In a real app, you would use JavaMailSender here
        log.info("MOCK EMAIL: Sending verification to [{}] with token [{}]", to, token);
        log.info("Link would be: http://localhost:3000/verify?token={}", token);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        log.info("MOCK EMAIL: Sending password reset to [{}] with token [{}]", to, token);
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("MOCK EMAIL: Resending verification email to [{}]", email);
    }
}