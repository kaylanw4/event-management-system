package com.example.eventmanagementsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter Tests")
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    private UserDetails userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Reset security context
        SecurityContextHolder.clearContext();

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

        validToken = "valid.jwt.token";

        // Mock SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("doFilterInternal Method Tests")
    class DoFilterInternalTests {

        @Test
        @DisplayName("Should set authentication when valid token is provided")
        void shouldSetAuthenticationWhenValidTokenIsProvided() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
            given(tokenProvider.getUsernameFromToken(validToken)).willReturn(userDetails.getUsername());
            given(userDetailsService.loadUserByUsername(userDetails.getUsername())).willReturn(userDetails);
            given(tokenProvider.validateToken(validToken, userDetails)).willReturn(true);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not set authentication when token is invalid")
        void shouldNotSetAuthenticationWhenTokenIsInvalid() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
            given(tokenProvider.getUsernameFromToken(validToken)).willReturn(userDetails.getUsername());
            given(userDetailsService.loadUserByUsername(userDetails.getUsername())).willReturn(userDetails);
            given(tokenProvider.validateToken(validToken, userDetails)).willReturn(false);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not set authentication when Authorization header is not provided")
        void shouldNotSetAuthenticationWhenAuthorizationHeaderIsNotProvided() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn(null);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(tokenProvider, never()).getUsernameFromToken(anyString());
            verify(userDetailsService, never()).loadUserByUsername(anyString());
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not set authentication when Authorization header does not start with Bearer")
        void shouldNotSetAuthenticationWhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn("Basic " + validToken);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(tokenProvider, never()).getUsernameFromToken(anyString());
            verify(userDetailsService, never()).loadUserByUsername(anyString());
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not set authentication and continue filter chain when exception occurs")
        void shouldNotSetAuthenticationAndContinueFilterChainWhenExceptionOccurs() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
            given(tokenProvider.getUsernameFromToken(validToken)).willThrow(new RuntimeException("Token error"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(securityContext, never()).setAuthentication(any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("JWT Extraction Tests")
    class JwtExtractionTests {

        @Test
        @DisplayName("Should extract JWT from Authorization header correctly")
        void shouldExtractJwtFromAuthorizationHeaderCorrectly() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
            given(tokenProvider.getUsernameFromToken(validToken)).willReturn(userDetails.getUsername());
            given(userDetailsService.loadUserByUsername(userDetails.getUsername())).willReturn(userDetails);
            given(tokenProvider.validateToken(validToken, userDetails)).willReturn(true);

            // Set up a mock UsernamePasswordAuthenticationToken to be compared with what's created in the filter
            UsernamePasswordAuthenticationToken expectedAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            expectedAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            // Verify tokenProvider was called with the correct token
            verify(tokenProvider).getUsernameFromToken(validToken);
            verify(tokenProvider).validateToken(validToken, userDetails);

            // Verify authentication was set
            verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should not extract JWT when Authorization header is empty")
        void shouldNotExtractJwtWhenAuthorizationHeaderIsEmpty() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn("");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(tokenProvider, never()).getUsernameFromToken(anyString());
            verify(filterChain).doFilter(request, response);
        }
    }
}