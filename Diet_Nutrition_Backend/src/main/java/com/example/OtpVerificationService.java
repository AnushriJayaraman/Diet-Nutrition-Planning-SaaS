package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OtpVerificationService {

    private static final Duration OTP_LIFETIME = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 5;

    private final ObjectMapper objectMapper;
    private final String brevoApiKey;
    private final String brevoSenderEmail;
    private final String brevoSenderName;
    private final SecureRandom secureRandom = new SecureRandom();
    private final byte[] otpHashKey = new byte[32];
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConcurrentHashMap<String, PendingEntry> pendingSignups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PendingCode> pendingPasswordResets = new ConcurrentHashMap<>();

    public OtpVerificationService(
        ObjectMapper objectMapper,
        @Value("${brevo.api-key:}") String brevoApiKey,
        @Value("${brevo.sender-email:}") String brevoSenderEmail,
        @Value("${brevo.sender-name:NutriCare}") String brevoSenderName
    ) {
        this.objectMapper = objectMapper;
        this.brevoApiKey = brevoApiKey;
        this.brevoSenderEmail = brevoSenderEmail;
        this.brevoSenderName = brevoSenderName;
        secureRandom.nextBytes(otpHashKey);
    }

    public synchronized String start(PendingSignupData data) {
        String email = normalizeEmail(data.email());
        String code = generateCode();
        PendingEntry entry = new PendingEntry(data, hashCode(code), Instant.now().plus(OTP_LIFETIME));
        deliver(data.email(), code);
        pendingSignups.put(email, entry);
        return maskEmail(data.email());
    }

    public synchronized String resend(String email) {
        String normalizedEmail = normalizeEmail(email);
        PendingEntry entry = pendingSignups.get(normalizedEmail);
        if (entry == null || entry.expiresAt.isBefore(Instant.now())) {
            pendingSignups.remove(normalizedEmail);
            throw new IllegalArgumentException("Start account verification again to request a new code");
        }
        if (entry.lastSentAt.plus(RESEND_COOLDOWN).isAfter(Instant.now())) {
            throw new IllegalArgumentException("Please wait 30 seconds before requesting another code");
        }

        String code = generateCode();
        deliver(entry.data.email(), code);
        entry.codeHash = hashCode(code);
        entry.expiresAt = Instant.now().plus(OTP_LIFETIME);
        entry.lastSentAt = Instant.now();
        entry.attempts = 0;
        return maskEmail(entry.data.email());
    }

    public long countPendingForRole(String role) {
        return pendingSignups.values().stream()
            .filter(entry -> entry.data.role().equals(role))
            .count();
    }

    public PendingSignupData verifyAndConsume(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        PendingEntry entry = pendingSignups.get(normalizedEmail);
        if (entry == null) {
            throw new IllegalArgumentException("No pending verification was found. Request a new code");
        }

        synchronized (entry) {
            if (entry.expiresAt.isBefore(Instant.now())) {
                pendingSignups.remove(normalizedEmail, entry);
                throw new IllegalArgumentException("Verification code expired. Request a new code");
            }
            if (++entry.attempts > MAX_ATTEMPTS) {
                pendingSignups.remove(normalizedEmail, entry);
                throw new IllegalArgumentException("Too many incorrect codes. Request a new code");
            }
            if (!MessageDigest.isEqual(entry.codeHash, hashCode(code))) {
                throw new IllegalArgumentException("Incorrect verification code");
            }
            pendingSignups.remove(normalizedEmail, entry);
            return entry.data;
        }
    }

    public synchronized void startPasswordReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        PendingCode existing = pendingPasswordResets.get(normalizedEmail);
        if (existing != null && existing.expiresAt.isAfter(Instant.now())
            && existing.lastSentAt.plus(RESEND_COOLDOWN).isAfter(Instant.now())) {
            return;
        }

        String code = generateCode();
        sendEmail(normalizedEmail, code, "NutriCare password reset");
        pendingPasswordResets.put(
            normalizedEmail,
            new PendingCode(hashCode(code), Instant.now().plus(OTP_LIFETIME))
        );
    }

    public void verifyPasswordReset(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        PendingCode entry = pendingPasswordResets.get(normalizedEmail);
        if (entry == null) {
            throw new IllegalArgumentException("Invalid or expired password reset code");
        }

        synchronized (entry) {
            if (entry.expiresAt.isBefore(Instant.now())) {
                pendingPasswordResets.remove(normalizedEmail, entry);
                throw new IllegalArgumentException("Invalid or expired password reset code");
            }
            if (++entry.attempts > MAX_ATTEMPTS) {
                pendingPasswordResets.remove(normalizedEmail, entry);
                throw new IllegalArgumentException("Too many incorrect codes. Request a new code");
            }
            if (!MessageDigest.isEqual(entry.codeHash, hashCode(code))) {
                throw new IllegalArgumentException("Incorrect verification code");
            }
            pendingPasswordResets.remove(normalizedEmail, entry);
        }
    }

    private void deliver(String destination, String code) {
        sendEmail(destination, code, "NutriCare account verification");
    }

    private void sendEmail(String destination, String code, String subject) {
        if (brevoApiKey.isBlank() || brevoSenderEmail.isBlank()) {
            throw new IllegalStateException(
                "Email OTP is not configured. Run .\\setup-local-secrets.ps1 once to save your Brevo sender email and API key, or set BREVO_API_KEY and BREVO_SENDER_EMAIL"
            );
        }

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                "sender", Map.of("name", brevoSenderName, "email", brevoSenderEmail),
                "to", List.of(Map.of("email", destination)),
                "subject", subject,
                "textContent", "Your NutriCare code is " + code + ". It expires in 5 minutes."
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.brevo.com/v3/smtp/email"))
                .timeout(Duration.ofSeconds(15))
                .header("api-key", brevoApiKey)
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Brevo rejected the verification email");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Email delivery was interrupted", exception);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Unable to send verification email through Brevo", exception);
        }
    }

    private String generateCode() {
        return String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
    }

    private byte[] hashCode(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(otpHashKey, "HmacSHA256"));
            return mac.doFinal(code.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to validate verification code", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? email : email.substring(0, 1) + "***" + email.substring(at);
    }

    private static final class PendingEntry {
        private final PendingSignupData data;
        private byte[] codeHash;
        private Instant expiresAt;
        private Instant lastSentAt = Instant.now();
        private int attempts;

        private PendingEntry(PendingSignupData data, byte[] codeHash, Instant expiresAt) {
            this.data = data;
            this.codeHash = codeHash;
            this.expiresAt = expiresAt;
        }
    }

    private static final class PendingCode {
        private final byte[] codeHash;
        private final Instant expiresAt;
        private final Instant lastSentAt = Instant.now();
        private int attempts;

        private PendingCode(byte[] codeHash, Instant expiresAt) {
            this.codeHash = codeHash;
            this.expiresAt = expiresAt;
        }
    }
}

record PendingSignupData(
    String name,
    String email,
    String passwordHash,
    String role
) {}
