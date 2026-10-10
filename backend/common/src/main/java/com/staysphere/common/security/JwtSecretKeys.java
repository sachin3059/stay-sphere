package com.staysphere.common.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public final class JwtSecretKeys {

    private JwtSecretKeys() {}

    /**
     * HS256 key from Base64-encoded secret, or UTF-8 passphrase if not valid Base64.
     * Use at least 32 characters for passphrases.
     */
    public static SecretKey hmacShaKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret is not configured");
        }
        return Keys.hmacShaKeyFor(resolveKeyBytes(secret.trim()));
    }

    static byte[] resolveKeyBytes(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
