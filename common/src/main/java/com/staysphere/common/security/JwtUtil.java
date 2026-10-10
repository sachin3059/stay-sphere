package com.staysphere.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private javax.crypto.SecretKey getSigningKey() {
        return JwtSecretKeys.hmacShaKey(secret);
    }

    public String generateToken(String email, String role) {
        return generateToken(email, role, true);
    }

    public String generateToken(String email, String role, boolean emailVerified) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("emailVerified", emailVerified)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Missing claim is treated as verified for tokens issued before email verification shipped.
     */
    public boolean extractEmailVerified(String token) {
        Boolean value = parseClaims(token).get("emailVerified", Boolean.class);
        return value == null || value;
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
