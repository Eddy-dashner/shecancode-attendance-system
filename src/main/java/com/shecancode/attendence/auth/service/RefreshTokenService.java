package com.shecancode.attendence.auth.service;

import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.RefreshToken;
import com.shecancode.attendence.auth.repository.RefreshTokenRepository;
import com.shecancode.attendence.registration.Exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Issues and rotates refresh tokens. Tokens are opaque, random strings stored
 * server-side (not self-contained JWTs) so a single token can be revoked without
 * needing a blacklist.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public RefreshToken issue(AppUser user) {
        RefreshToken token = RefreshToken.builder()
                .token(generateSecureToken())
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();
        return refreshTokenRepository.save(token);
    }

    /**
     * Validates the given raw token and, if valid, revokes it and issues a
     * replacement for the same user (rotation: each refresh token is single-use).
     */
    @Transactional
    public RefreshToken rotate(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token."));

        if (!existing.isValid()) {
            throw new InvalidRefreshTokenException("Refresh token is expired or has been revoked.");
        }

        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);

        return issue(existing.getUser());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
