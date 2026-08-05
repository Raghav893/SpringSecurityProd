package com.raghav.springsecurityprod.service;

import com.raghav.springsecurityprod.entity.ForgotPasswordToken;
import com.raghav.springsecurityprod.entity.User;
import com.raghav.springsecurityprod.exceptions.EmailNotFoundException;
import com.raghav.springsecurityprod.repo.ForgotPasswordTokenRepository;
import com.raghav.springsecurityprod.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;
    private final UserRepository userRepository;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailService emailService;
    @Value("${app.base-url}")
    private String baseUrl;
    private static final long  EXPIRY_MS = 6*60*100;

    @Transactional
    public void issueVerificationToken(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("Email Not found"));

        forgotPasswordTokenRepository.invalidateAllUnusedForUser(user);

        ForgotPasswordToken token = ForgotPasswordToken.builder()
                .used(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(EXPIRY_MS))
                .user(user)
                .tokenHash(hash(generateSecureRandomToken()))
                .build();
        String forgotPasswordUrl = baseUrl + "/api/auth/forgot-password?token="+token;

        emailService.sendForgotPasswordEmail(user.getEmail(),forgotPasswordUrl);
    }
    private String generateSecureRandomToken(){
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private String hash(String rawToken){
        try {
            MessageDigest digest =  MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
