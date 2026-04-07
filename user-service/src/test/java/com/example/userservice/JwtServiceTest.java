package com.example.userservice;

import static org.junit.jupiter.api.Assertions.*;

import com.example.userservice.security.jwt.JwtService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET =
        "test-secret-test-secret-test-secret-test-secret";
    private static final long ACCESS_TOKEN_EXP_MS = 900_000;
    private static final String ISSUER = "test-issuer";

    private final JwtService jwtService =
        new JwtService(SECRET, ACCESS_TOKEN_EXP_MS, ISSUER);

    @Test
    void generatesValidTokenAndExtractsClaims() {
        UUID userId = UUID.randomUUID();
        String username = "alice";

        String token = jwtService.generateAccessToken(userId, username);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(username, jwtService.extractUsername(token));
        Instant expiry = jwtService.extractExpiration(token);
        assertNotNull(expiry);
        assertTrue(expiry.isAfter(Instant.now()));
    }

    @Test
    void invalidTokenReturnsFalse() {
        String invalidToken = "not-a-valid-jwt";

        assertFalse(jwtService.isTokenValid(invalidToken));
    }
}
