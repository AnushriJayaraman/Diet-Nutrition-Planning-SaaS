package com.example;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

    private static final int PASSWORD_HASH_ITERATIONS = 120_000;
    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        byte[] hash = derivePasswordHash(password, salt, PASSWORD_HASH_ITERATIONS);
        return "pbkdf2$" + PASSWORD_HASH_ITERATIONS + "$"
            + Base64.getEncoder().encodeToString(salt) + "$"
            + Base64.getEncoder().encodeToString(hash);
    }

    public boolean matches(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("pbkdf2$")) {
            return false;
        }

        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = derivePasswordHash(password, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] derivePasswordHash(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash account password", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
