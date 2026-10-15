package com.example;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final AccountRepository accountRepository;
    private final PasswordHashService passwordHashService;
    private final String configuredName;
    private final String configuredEmail;
    private final String configuredPassword;
    private final boolean resetPassword;

    public AdminAccountInitializer(
        AccountRepository accountRepository,
        PasswordHashService passwordHashService,
        @Value("${app.auth.bootstrap-admin.name:Administrator}") String configuredName,
        @Value("${app.auth.bootstrap-admin.email:}") String configuredEmail,
        @Value("${app.auth.bootstrap-admin.password:}") String configuredPassword,
        @Value("${app.auth.bootstrap-admin.reset-password:false}") boolean resetPassword
    ) {
        this.accountRepository = accountRepository;
        this.passwordHashService = passwordHashService;
        this.configuredName = configuredName;
        this.configuredEmail = configuredEmail;
        this.configuredPassword = configuredPassword;
        this.resetPassword = resetPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isBlank(configuredEmail) && isBlank(configuredPassword)) {
            logger.warn("No bootstrap admin configured. Set APP_AUTH_BOOTSTRAP_ADMIN_EMAIL and "
                + "APP_AUTH_BOOTSTRAP_ADMIN_PASSWORD to create the first admin account.");
            return;
        }
        if (isBlank(configuredEmail) || isBlank(configuredPassword)) {
            throw new IllegalStateException(
                "Both APP_AUTH_BOOTSTRAP_ADMIN_EMAIL and APP_AUTH_BOOTSTRAP_ADMIN_PASSWORD must be set."
            );
        }

        String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalStateException("APP_AUTH_BOOTSTRAP_ADMIN_EMAIL must be a valid email address.");
        }
        if (isBlank(configuredName) || configuredName.trim().length() > 120) {
            throw new IllegalStateException("APP_AUTH_BOOTSTRAP_ADMIN_NAME must contain 1 to 120 characters.");
        }
        if (!isStrongPassword(configuredPassword)) {
            throw new IllegalStateException(
                "APP_AUTH_BOOTSTRAP_ADMIN_PASSWORD must be at least 8 characters and include "
                    + "uppercase, lowercase, numeric, and special characters."
            );
        }

        AccountEntity configuredAccount = accountRepository.findByEmail(email).orElse(null);
        if (configuredAccount != null) {
            if (!"admin".equals(configuredAccount.getRole())) {
                throw new IllegalStateException(
                    "The configured bootstrap admin email already belongs to a non-admin account."
                );
            }
            if (resetPassword) {
                configuredAccount.setPasswordHash(passwordHashService.hash(configuredPassword));
                accountRepository.save(configuredAccount);
                logger.warn("Reset the configured admin password. Disable APP_AUTH_BOOTSTRAP_ADMIN_RESET_PASSWORD "
                    + "after this deployment.");
            } else {
                logger.info("The configured admin account already exists; password reset was skipped.");
            }
            return;
        }

        if (accountRepository.countByRole("admin") > 0) {
            logger.warn("An admin account exists with a different email; bootstrap admin creation was skipped.");
            return;
        }

        accountRepository.save(new AccountEntity(
            configuredName.trim(),
            email,
            passwordHashService.hash(configuredPassword),
            "admin"
        ));
        logger.info("Created the initial admin account from deployment configuration.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8
            && password.matches(".*[a-z].*")
            && password.matches(".*[A-Z].*")
            && password.matches(".*\\d.*")
            && password.matches(".*[^A-Za-z0-9].*");
    }
}
