package com.raghav.springsecurityprod.service;

import com.raghav.springsecurityprod.entity.RefreshToken;
import com.raghav.springsecurityprod.entity.User;
import com.raghav.springsecurityprod.exceptions.InvalidTokenException;
import com.raghav.springsecurityprod.repo.RefreshTokenRepository;

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

public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiry-ms}")
    private Long refreshExpiryMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public String issueRefreshToken(User user){

        String rawToken = generateSecureRandomToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(Instant.now().plusMillis(refreshExpiryMs))
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RotationResult rotate(String rawToken){
        String tokenHash = hash(rawToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(()-> new InvalidTokenException("Refresh token not recognised "));
        if (existing.isRevoked()){
            // Reuse of a revoked token = possible theft. Revoke the entire chain for this user.
            revokeAllforUser(existing.getUser());
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }
        existing.setRevoked(true);
        String newRawToken = issueRefreshToken(existing.getUser());
        existing.setReplacedByTokenId(
                refreshTokenRepository.findByTokenHash(hash(newRawToken)).orElseThrow().getId()
        );
        refreshTokenRepository.save(existing);

        return new RotationResult(newRawToken, existing.getUser());


    }
    public void revoke(String token){
        refreshTokenRepository.findByTokenHash(hash(token))
                .ifPresent(t->{t.setRevoked(true);refreshTokenRepository.save(t);});
    }
    public void revokeAllforUser(User user){
        refreshTokenRepository.revokeAllByUser(user);
    }

    private String generateSecureRandomToken(){
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private String hash(String raw){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    public record RotationResult(String newRawToken, User user) {}
}
