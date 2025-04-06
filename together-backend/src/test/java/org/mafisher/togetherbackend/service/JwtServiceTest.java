package org.mafisher.togetherbackend.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mafisher.togetherbackend.service.impl.JwtServiceImpl;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;
    private final String base64Key = "testSecretKey12345678901234567890123456789012345678901234567890123456789012";

    @BeforeEach
    void setUp() {
        this.jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secretKey", base64Key);
    }

    private String generateTokenWithCustomExpiration(String username, long offsetMillis) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        Date now = new Date(System.currentTimeMillis());
        Date expires = new Date(System.currentTimeMillis() + offsetMillis);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expires)
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void generateToken_extractUserName_returnsCorrectUsername() {
        String username = "testuser";
        String token = jwtService.generateToken(username);
        String extractedUsername = jwtService.extractUserName(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractUserName_withTamperedToken_throwsException() {
        String validToken = jwtService.generateToken("testuser");
        String[] parts = validToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tamperedSignature";
        assertThrows(JwtException.class, () -> jwtService.extractUserName(tamperedToken));
    }

    @Test
    void extractUserName_withExpiredToken_returnsUsername() {
        String username = "testuser";
        String expiredToken = generateTokenWithCustomExpiration(username, 3600000);
        String extractedUsername = jwtService.extractUserName(expiredToken);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateJwtToken_withValidToken_returnsTrue() {
        String token = jwtService.generateToken("testuser");
        assertTrue(jwtService.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_withExpiredToken_returnsFalse() {
        String expiredToken = generateTokenWithCustomExpiration("testuser", -3600000);
        assertFalse(jwtService.validateJwtToken(expiredToken));
    }

    @Test
    void validateJwtToken_withInvalidSignature_returnsFalse() {
        String otherKey = "otherSecretKey123456789012345678901234567890123456789012345678901234567890";
        byte[] otherKeyBytes = Base64.getDecoder().decode(otherKey);
        SecretKey differentKey = Keys.hmacShaKeyFor(otherKeyBytes);
        String token = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000))
                .signWith(differentKey)
                .compact();
        assertFalse(jwtService.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_withMalformedToken_returnsFalse() {
        assertFalse(jwtService.validateJwtToken("malformed.token"));
    }

    @Test
    void validateJwtToken_withEmptyToken_returnsFalse() {
        assertFalse(jwtService.validateJwtToken(""));
    }

    @Test
    void validateJwtToken_withNullToken_returnsFalse() {
        assertFalse(jwtService.validateJwtToken(null));
    }
}

