package com.ines.skillmatch_auth_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
/**
 * Service for handling JSON Web Token (JWT) operations.
 *
 * This service provides methods for:
 * - Generating JWT tokens from user details
 * - Extracting claims (email, userId, role) from tokens
 * - Validating token integrity and expiration
 *
 * The tokens are signed using HMAC-SHA256 with a Base64-encoded secret key
 * configured in the application properties.
 */
@Service
public class JwtService {
    /**
     * The Base64-encoded secret key used to sign JWT tokens.
     * Injected from application properties (jwt.secret).
     */
    @Value("${jwt.secret}")
    private String secret;
    /**
     * The expiration time for JWT tokens in milliseconds.
     * Injected from application properties (jwt.expiration).
     */
    @Value("${jwt.expiration}")
    private Long expiration;
    /**
     * Generates a JWT token for the authenticated user.
     *
     * The token includes the following claims:
     * - userId: the user's internal ID
     * - role: the user's role (CANDIDAT, RECRUTEUR, ADMIN)
     * - email: the user's email address
     * - sub (subject): the username (email)
     * - iat (issued at): current timestamp
     * - exp (expiration): current timestamp + configured expiration duration
     *
     * The token is signed with the HMAC-SHA256 secret key.
     *
     * @param userDetails the authenticated user details
     * @return the signed JWT token as a compact string
     */
    public String generateToken(UserDetailsImpl userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getId());
        claims.put("role", userDetails.getRole().name());
        claims.put("email", userDetails.getEmail());

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the email address stored in the subject claim
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }
    /**
     * Extracts the user's role from a JWT token.
     *
     * @param token the JWT token
     * @return the role name (e.g., "CANDIDAT", "RECRUTEUR", "ADMIN")
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    /**
     * Validates a JWT token by attempting to parse and verify it.
     *
     * A token is considered valid if:
     * - Its signature matches the secret key
     * - It has not expired
     * - It is structurally well-formed
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise (including any parsing errors)
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    /**
     * Extracts all claims from a JWT token by parsing and verifying it.
     *
     * This method validates:
     * - The token's signature
     * - The token's expiration
     * - The token's structural integrity
     *
     * @param token the JWT token
     * @return the Claims object containing all payload claims
     * @throws JwtException if the token is invalid, expired, or malformed
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())                 // Set the signing key for verification
                .build()                                     // Build the parser
                .parseSignedClaims(token)                    // Parse and verify the token
                .getPayload();                               // Extract the claims body
    }
    /**
     * Builds the HMAC-SHA256 signing key from the Base64-encoded secret.
     *
     * The secret is decoded from Base64 to bytes and then converted to a
     * SecretKey using the HMAC-SHA256 algorithm.
     *
     * @return the SecretKey used for signing and verification
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
