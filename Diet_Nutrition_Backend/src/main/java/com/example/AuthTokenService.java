package com.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private static final long TOKEN_LIFETIME_SECONDS = 12 * 60 * 60;
    private static final int MIN_SECRET_BYTES = 32;
    private final byte[] secret;

    public AuthTokenService(@Value("${app.auth.token-secret:}") String secret) {
        byte[] configuredSecret = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (configuredSecret.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                "APP_AUTH_TOKEN_SECRET must be configured with at least 32 bytes before the application can start"
            );
        }
        this.secret = configuredSecret;
    }

    public String issue(String email, String role) {
        long expiresAt = Instant.now().getEpochSecond() + TOKEN_LIFETIME_SECONDS;
        String encodedEmail = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(email.getBytes(StandardCharsets.UTF_8));
        String payload = encodedEmail + "." + role + "." + expiresAt;
        return payload + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(sign(payload));
    }

    public AuthenticatedUser validate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 4) {
            return null;
        }
        String payload = parts[0] + "." + parts[1] + "." + parts[2];
        try {
            byte[] expected = sign(payload);
            byte[] actual = Base64.getUrlDecoder().decode(parts[3]);
            if (!MessageDigest.isEqual(expected, actual) || Long.parseLong(parts[2]) <= Instant.now().getEpochSecond()) {
                return null;
            }
            String email = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String role = parts[1];
            if (email.isBlank() || !(role.equals("admin") || role.equals("nutritionist") || role.equals("patient"))) {
                return null;
            }
            return new AuthenticatedUser(email, role);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign authentication token", exception);
        }
    }
}
