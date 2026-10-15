package com.example;

import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origin:*}")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AccountRepository accountRepository;
    private final OtpVerificationService otpVerificationService;
    private final AuthTokenService authTokenService;
    private final PasswordHashService passwordHashService;

    @Autowired
    public AuthController(
        AccountRepository accountRepository,
        OtpVerificationService otpVerificationService,
        AuthTokenService authTokenService,
        PasswordHashService passwordHashService
    ) {
        this.accountRepository = accountRepository;
        this.otpVerificationService = otpVerificationService;
        this.authTokenService = authTokenService;
        this.passwordHashService = passwordHashService;
    }

    @PostMapping({"/signup", "/signup/otp"})
    public ResponseEntity<Map<String, Object>> startSignup(@RequestBody SignupOtpRequest request) {
        if (request == null || isBlank(request.name()) || isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name, email and password are required"));
        }

        String normalizedEmail = normalizeEmail(request.email());
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Enter a valid email address"));
        }
        if (accountRepository.findByEmail(normalizedEmail).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "User with this email already exists"));
        }

        if (!isStrongPassword(request.password())) {
            return ResponseEntity.badRequest().body(Map.of(
                "message",
                "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character."
            ));
        }
        if (!isBlank(request.confirmPassword()) && !request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
        }

        String role = request.role() == null || request.role().isBlank() ? "patient" : request.role().trim().toLowerCase(Locale.ROOT);
        if (!isPublicSignupRole(role)) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Public signup is available only for patients and nutritionists"
            ));
        }
        try {
            String maskedDestination = otpVerificationService.start(new PendingSignupData(
                request.name().trim(),
                normalizedEmail,
                passwordHashService.hash(request.password()),
                role
            ));
            return ResponseEntity.ok(Map.of(
                "message", "Verification code sent",
                "destination", maskedDestination
            ));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/signup/resend-otp")
    public ResponseEntity<Map<String, Object>> resendSignupCode(@RequestBody ResendOtpRequest request) {
        if (request == null || isBlank(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        try {
            String destination = otpVerificationService.resend(normalizeEmail(request.email()));
            return ResponseEntity.ok(Map.of(
                "message", "A new verification code was sent",
                "destination", destination
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", exception.getMessage()));
        }
    }

    @PostMapping("/signup/verify")
    public synchronized ResponseEntity<Map<String, Object>> verifySignup(@RequestBody VerifySignupRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.code())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and verification code are required"));
        }

        PendingSignupData signup;
        try {
            signup = otpVerificationService.verifyAndConsume(normalizeEmail(request.email()), request.code().trim());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }

        if (accountRepository.findByEmail(signup.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "User with this email already exists"));
        }
        AccountEntity user = accountRepository.save(new AccountEntity(
            signup.name(),
            signup.email(),
            signup.passwordHash(),
            signup.role()
        ));

        return ResponseEntity.ok(Map.of(
            "message", "User registered successfully",
            "user", publicUser(user)
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }
        if (isBlank(request.role())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please select a patient or nutritionist role"));
        }
        String requestedRole = request.role().trim().toLowerCase(Locale.ROOT);
        if (!isPublicSignupRole(requestedRole)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Please select a patient or nutritionist role"));
        }

        String normalizedEmail = normalizeEmail(request.email());
        AccountEntity user = accountRepository.findByEmail(normalizedEmail).orElse(null);

        if (user == null
            || !passwordHashService.matches(request.password(), user.getPasswordHash())
            || !requestedRole.equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
        }
        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "This account is disabled. Contact your clinic administrator."));
        }

        if (!isSupportedRole(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "This account role cannot sign in"));
        }

        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "user", publicUser(user),
            "token", authTokenService.issue(user.getEmail(), user.getRole())
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @RequestBody ChangePasswordRequest request
    ) {
        AuthenticatedUser authenticated = authorization != null && authorization.startsWith("Bearer ")
            ? authTokenService.validate(authorization.substring(7))
            : null;
        if (authenticated == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "A valid login is required"));
        }
        AccountEntity user = accountRepository.findByEmail(normalizeEmail(authenticated.email())).orElse(null);
        if (user == null || !user.isEnabled() || !user.getRole().equals(authenticated.role())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "This account is disabled or no longer active"));
        }
        if (request == null || isBlank(request.currentPassword()) || isBlank(request.newPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current and new passwords are required"));
        }
        if (!passwordHashService.matches(request.currentPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Current password is incorrect"));
        }
        if (!isStrongPassword(request.newPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                "message",
                "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character."
            ));
        }
        user.setPasswordHash(passwordHashService.hash(request.newPassword()));
        accountRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        if (request == null || isBlank(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        String email = normalizeEmail(request.email());
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Enter a valid email address"));
        }

        if (accountRepository.findByEmail(email).isPresent()) {
            try {
                otpVerificationService.startPasswordReset(email);
            } catch (IllegalStateException exception) {
                logger.error("Unable to send password reset email through Brevo", exception);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Unable to send the password reset email. Please try again later."));
            }
        }

        return ResponseEntity.ok(Map.of(
            "message", "If an account exists for this email, a password reset code has been sent."
        ));
    }

    @PostMapping("/password-reset/confirm")
    public synchronized ResponseEntity<Map<String, Object>> confirmPasswordReset(
        @RequestBody ConfirmPasswordResetRequest request
    ) {
        if (request == null || isBlank(request.email()) || isBlank(request.code()) || isBlank(request.newPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email, code and new password are required"));
        }
        if (!isStrongPassword(request.newPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                "message",
                "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one number and one special character."
            ));
        }

        String email = normalizeEmail(request.email());
        AccountEntity user = accountRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired password reset code"));
        }

        try {
            otpVerificationService.verifyPasswordReset(email, request.code().trim());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }

        user.setPasswordHash(passwordHashService.hash(request.newPassword()));
        accountRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now sign in."));
    }

    @PostMapping("/admin-login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody AdminLoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admin ID and password are required"));
        }

        String normalizedEmail = normalizeEmail(request.email());
        AccountEntity user = accountRepository.findByEmail(normalizedEmail).orElse(null);

        if (user == null || !passwordHashService.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid admin ID or password"));
        }
        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "This account is disabled. Contact your clinic administrator."));
        }
        if (!user.getRole().equals("admin")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "This account does not have administrator access"));
        }

        return ResponseEntity.ok(Map.of(
            "message", "Admin login successful",
            "user", publicUser(user),
            "token", authTokenService.issue(user.getEmail(), user.getRole())
        ));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8
            && password.matches(".*[a-z].*")
            && password.matches(".*[A-Z].*")
            && password.matches(".*\\d.*")
            && password.matches(".*[^A-Za-z0-9].*");
    }

    private boolean isSupportedRole(String role) {
        return role.equals("patient") || role.equals("nutritionist") || role.equals("admin");
    }

    private boolean isPublicSignupRole(String role) {
        return role.equals("patient") || role.equals("nutritionist");
    }

    private Map<String, Object> publicUser(AccountEntity user) {
        return Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole()
        );
    }

}

record SignupOtpRequest(
    String name,
    String email,
    String password,
    String confirmPassword,
    String role
) {}

record ResendOtpRequest(String email) {}

record VerifySignupRequest(String email, String code) {}

record LoginRequest(String email, String password, String role) {}

record PasswordResetRequest(String email) {}

record ConfirmPasswordResetRequest(String email, String code, String newPassword) {}

record AdminLoginRequest(String email, String password) {}

record ChangePasswordRequest(String currentPassword, String newPassword) {}

record User(long id, String name, String email, String password, String role) {}
