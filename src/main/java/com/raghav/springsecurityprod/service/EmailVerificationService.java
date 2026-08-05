package com.raghav.springsecurityprod.service;

import com.raghav.springsecurityprod.entity.EmailVerificationToken;
import com.raghav.springsecurityprod.entity.User;
import com.raghav.springsecurityprod.exceptions.InvalidVerificationTokenException;
import com.raghav.springsecurityprod.repo.EmailVerificationTokenRepository;
import com.raghav.springsecurityprod.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final UserRepository userRepository;


    @Value("${app.base-url}")
    private String baseUrl;
    private static final long EXPIRY_MS = 24 * 60 * 60 * 100;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void issueVerificationToken(User user) {
        emailVerificationTokenRepository.invalidateAllUnusedForUser(user);
        String rawToken = generateSecureRandomToken();
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plusMillis(EXPIRY_MS))
                .used(false)
                .createdAt(Instant.now())
                .build();
        emailVerificationTokenRepository.save(emailVerificationToken);
        String verificationLink = baseUrl + "/api/auth/verify-email?token=" + rawToken;
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidVerificationTokenException("Verification link is invalid"));
        if (token.isUsed()) {

            throw new InvalidVerificationTokenException("Verification link has already been used");

        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidVerificationTokenException("Verification link has expired");

        }
        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
    }

    @Transactional
    public void resendVerification(User user) {
        if (user.isEmailVerified()) {
            return;
        }
        issueVerificationToken(user);
    }


    private String generateSecureRandomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }

    }
}