package com.example.userservice.service;

import com.example.userservice.exception.UnauthorizedException;
import com.example.userservice.model.RefreshToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.RefreshTokenRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationMs;

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        @Value(
            "${app.jwt.refresh-token-expiration-ms}"
        ) long refreshTokenExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpiryDate(
            Instant.now().plusMillis(refreshTokenExpirationMs)
        );
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
            .findByToken(token)
            .orElseThrow(() ->
                new UnauthorizedException("Invalid refresh token")
            );

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String token) {
        RefreshToken existing = validateRefreshToken(token);
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        return createRefreshToken(existing.getUser());
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository
            .findByToken(token)
            .ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
    }

    private String generateSecureToken() {
        byte[] buffer = new byte[64];
        SecureRandom random = new SecureRandom();
        random.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}
