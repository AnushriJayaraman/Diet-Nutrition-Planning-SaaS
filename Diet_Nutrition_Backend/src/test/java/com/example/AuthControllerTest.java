package com.example;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    private static final String TEST_DATA_FILE = Path.of(
        "target", "test-data", "auth-" + UUID.randomUUID() + ".json"
    ).toString();

    @DynamicPropertySource
    static void useIsolatedDataFile(DynamicPropertyRegistry registry) {
        registry.add("app.data.file", () -> TEST_DATA_FILE);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthTokenService authTokenService;

    @MockitoBean
    private OtpVerificationService otpVerificationService;

    @Test
    void publicSignupRejectsAdminButAllowsNutritionistAccounts() throws Exception {
        when(otpVerificationService.start(any(PendingSignupData.class))).thenReturn("u***@example.com");

        mockMvc.perform(post("/api/auth/signup/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "New Admin",
                    "email", "open-admin@example.com",
                    "password", "AdminPass1!",
                    "role", "admin"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Public signup is available only for patients and nutritionists"));

        mockMvc.perform(post("/api/auth/signup/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "New Nutritionist",
                    "email", "open-nutritionist-" + UUID.randomUUID() + "@example.com",
                    "password", "NutritionPass1!",
                    "role", "nutritionist"
                ))))
            .andExpect(status().isOk());
        verify(otpVerificationService, org.mockito.Mockito.times(1)).start(any(PendingSignupData.class));
    }

    @Test
    void signupRejectsMismatchedPasswordConfirmation() throws Exception {
        when(otpVerificationService.start(any(PendingSignupData.class))).thenReturn("a***@example.com");

        mockMvc.perform(post("/api/auth/signup/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Mismatch User",
                    "email", "mismatch@example.com",
                    "password", "StrongPass1!",
                    "confirmPassword", "WrongPass1!",
                    "role", "patient"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    @Test
    void signupRequiresOtpVerificationBeforeLogin() throws Exception {
        AtomicReference<PendingSignupData> pendingSignup = new AtomicReference<>();
        when(otpVerificationService.start(any(PendingSignupData.class))).thenAnswer(invocation -> {
            pendingSignup.set(invocation.getArgument(0));
            return "a***@example.com";
        });
        when(otpVerificationService.verifyAndConsume(eq("alice@example.com"), eq("123456")))
            .thenAnswer(invocation -> pendingSignup.get());

        Map<String, String> signupRequest = Map.of(
            "name", "Alice Johnson",
            "email", "alice@example.com",
            "password", "StrongPass1!",
            "role", "patient"
        );

        mockMvc.perform(post("/api/auth/signup/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Verification code sent"))
            .andExpect(jsonPath("$.destination").value("a***@example.com"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "alice@example.com",
                    "password", "StrongPass1!",
                    "role", "patient"
                ))))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/signup/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "alice@example.com",
                    "code", "123456"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "alice@example.com",
                    "password", "StrongPass1!",
                    "role", "patient"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value("patient"))
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/auth/admin-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "alice@example.com",
                    "password", "StrongPass1!"
                ))))
            .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanChangePasswordAfterVerifyingCurrentPassword() throws Exception {
        String email = "password-change-" + UUID.randomUUID() + "@example.com";
        accountRepository.save(new AccountEntity("Password User", email, passwordHash("OldPass1!"), "patient"));
        String token = authTokenService.issue(email, "patient");

        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "currentPassword", "OldPass1!",
                    "newPassword", "NewPass2!"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password changed successfully"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", "NewPass2!",
                    "role", "patient"
                ))))
            .andExpect(status().isOk());
    }

    @Test
    void existingAdminCanUseSeparateLoginWithoutRoleInput() throws Exception {
        String email = "clinic-admin-" + UUID.randomUUID() + "@example.com";
        accountRepository.save(new AccountEntity("Clinic Admin", email, passwordHash("AdminPass1!"), "admin"));

        mockMvc.perform(post("/api/auth/admin-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", "AdminPass1!"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value("admin"));
    }

    private String passwordHash(String password) throws GeneralSecurityException {
        byte[] salt = "test-salt-for-admin".getBytes(StandardCharsets.UTF_8);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120_000, 256);
        byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        spec.clearPassword();
        return "pbkdf2$120000$" + Base64.getEncoder().encodeToString(salt) + "$"
            + Base64.getEncoder().encodeToString(hash);
    }

    @Test
    void passwordResetRequiresEmailCodeAndChangesThePassword() throws Exception {
        AtomicReference<PendingSignupData> pendingSignup = new AtomicReference<>();
        when(otpVerificationService.start(any(PendingSignupData.class))).thenAnswer(invocation -> {
            pendingSignup.set(invocation.getArgument(0));
            return "p***@example.com";
        });
        when(otpVerificationService.verifyAndConsume(eq("password-reset@example.com"), eq("123456")))
            .thenAnswer(invocation -> pendingSignup.get());

        mockMvc.perform(post("/api/auth/signup/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Password Reset User",
                    "email", "password-reset@example.com",
                    "password", "StrongPass1!",
                    "role", "patient"
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/signup/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "password-reset@example.com",
                    "code", "123456"
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "password-reset@example.com"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(
                "If an account exists for this email, a password reset code has been sent."
            ));
        verify(otpVerificationService).startPasswordReset("password-reset@example.com");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "password-reset@example.com",
                    "code", "654321",
                    "newPassword", "NewStrongPass2!"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password reset successfully. You can now sign in."));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "password-reset@example.com",
                    "password", "StrongPass1!",
                    "role", "patient"
                ))))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "password-reset@example.com",
                    "password", "NewStrongPass2!",
                    "role", "patient"
                ))))
            .andExpect(status().isOk());
    }

    @Test
    void passwordResetRequestDoesNotRevealWhetherAccountExists() throws Exception {
        String responseMessage = "If an account exists for this email, a password reset code has been sent.";

        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "unknown@example.com"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(responseMessage));

        verify(otpVerificationService, never()).startPasswordReset("unknown@example.com");
    }

    @Test
    void loginAcceptsPatientRoleSelectedByUser() throws Exception {
        String email = "patient-role-login-" + UUID.randomUUID() + "@example.com";
        accountRepository.save(new AccountEntity("Selected Patient", email, passwordHash("PatientPass1!"), "patient"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", "PatientPass1!",
                    "role", "patient"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value("patient"))
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginAcceptsNutritionistRoleSelectedByUser() throws Exception {
        String email = "nutritionist-role-login-" + UUID.randomUUID() + "@example.com";
        accountRepository.save(new AccountEntity("Selected Nutritionist", email, passwordHash("NutritionPass1!"), "nutritionist"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", "NutritionPass1!",
                    "role", "nutritionist"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value("nutritionist"))
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginRejectsRoleThatDoesNotMatchTheAccount() throws Exception {
        String email = "mismatched-role-" + UUID.randomUUID() + "@example.com";
        accountRepository.save(new AccountEntity("Role Mismatch Patient", email, passwordHash("PatientPass1!"), "patient"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", "PatientPass1!",
                    "role", "nutritionist"
                ))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginRequiresRoleSelection() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "email", "patient@example.com",
                    "password", "PatientPass1!"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Please select a patient or nutritionist role"));
    }
}
