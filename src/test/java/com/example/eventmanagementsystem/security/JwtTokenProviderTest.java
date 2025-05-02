package com.example.eventmanagementsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    private UserDetails userDetails;
    private String jwtSecret;
    private long jwtExpirationInMs;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        // Setup user details with authorities
        Collection<SimpleGrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        userDetails = new User(
                "testuser",
                "password",
                true,
                true,
                true,
                true,
                authorities
        );

        // Set JWT properties via reflection
        jwtSecret = "thisIsAVerySecureSecretKeyForTestingPurposesOnlyDoNotUseInProduction";
        jwtExpirationInMs = 3600000; // 1 hour

        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", jwtExpirationInMs);

        // Mock authentication
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Generate signing key once for tests
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate token with correct claims")
        void shouldGenerateTokenWithCorrectClaims() {
            // When
            String token = tokenProvider.generateToken(authentication);

            // Then
            assertNotNull(token);
            assertFalse(token.isEmpty());

            // Verify token structure and claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            assertEquals(userDetails.getUsername(), claims.getSubject());
            assertNotNull(claims.getIssuedAt());
            assertNotNull(claims.getExpiration());

            // Expiration should be around 1 hour from now
            long expirationTimeInMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
            long expectedExpirationInMs = jwtExpirationInMs;

            // Allow for a small margin of error (up to 10 seconds)
            long marginOfErrorInMs = 10000;
            assertTrue(Math.abs(expirationTimeInMs - expectedExpirationInMs) < marginOfErrorInMs);
        }
    }

    @Nested
    @DisplayName("Username Extraction Tests")
    class UsernameExtractionTests {

        @Test
        @DisplayName("Should extract username from token correctly")
        void shouldExtractUsernameFromTokenCorrectly() {
            // Given
            String token = tokenProvider.generateToken(authentication);

            // When
            String extractedUsername = tokenProvider.getUsernameFromToken(token);

            // Then
            assertEquals(userDetails.getUsername(), extractedUsername);
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate token with matching username")
        void shouldValidateTokenWithMatchingUsername() {
            // Given
            String token = tokenProvider.generateToken(authentication);

            // When
            boolean isValid = tokenProvider.validateToken(token, userDetails);

            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should not validate token with different username")
        void shouldNotValidateTokenWithDifferentUsername() {
            // Given
            String token = tokenProvider.generateToken(authentication);

            // Create user details with different username
            UserDetails differentUser = new User(
                    "different_user",
                    "password",
                    true, true, true, true,
                    Collections.emptyList()
            );

            // When
            boolean isValid = tokenProvider.validateToken(token, differentUser);

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should not validate expired token")
        void shouldNotValidateExpiredToken() throws Exception {
            // Given
            // Set a very short expiration for this test
            long shortExpiration = 1; // 1ms
            ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", shortExpiration);

            String token = tokenProvider.generateToken(authentication);

            // Wait for token to expire
            TimeUnit.MILLISECONDS.sleep(10);

            // Reset original expiration
            ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", jwtExpirationInMs);

            // When & Then
            assertThrows(ExpiredJwtException.class, () -> {
                tokenProvider.getUsernameFromToken(token);
            });
        }
    }

    @Nested
    @DisplayName("Expiration Tests")
    class ExpirationTests {

        @Test
        @DisplayName("Should get correct expiration date from token")
        void shouldGetCorrectExpirationDateFromToken() {
            // Given
            String token = tokenProvider.generateToken(authentication);

            // When
            Date expirationDate = tokenProvider.getExpirationDateFromToken(token);

            // Then
            assertNotNull(expirationDate);

            // Calculate expected expiration time
            long currentTimeMillis = System.currentTimeMillis();
            long expectedExpirationMs = currentTimeMillis + jwtExpirationInMs;

            // Allow for a small margin of error (10 seconds)
            long marginOfErrorInMs = 10000;
            assertTrue(Math.abs(expirationDate.getTime() - expectedExpirationMs) < marginOfErrorInMs);
        }
    }
}