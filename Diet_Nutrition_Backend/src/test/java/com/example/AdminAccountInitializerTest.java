package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.mock;

class AdminAccountInitializerTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final PasswordHashService passwordHashService = new PasswordHashService();

    @Test
    void createsInitialAdminUsingAHashedPassword() {
        when(accountRepository.countByRole("admin")).thenReturn(0L);
        when(accountRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        AdminAccountInitializer initializer = initializer(" Admin@Example.com ", "AdminPass1!");

        initializer.run(null);

        ArgumentCaptor<AccountEntity> savedAccount = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(savedAccount.capture());
        AccountEntity admin = savedAccount.getValue();
        assertEquals("Administrator", admin.getName());
        assertEquals("admin@example.com", admin.getEmail());
        assertEquals("admin", admin.getRole());
        assertTrue(passwordHashService.matches("AdminPass1!", admin.getPasswordHash()));
    }

    @Test
    void doesNotCreateAnotherAdminWhenOneAlreadyExists() {
        when(accountRepository.countByRole("admin")).thenReturn(1L);

        initializer("admin@example.com", "AdminPass1!").run(null);

        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    void resetsPasswordOnlyWhenExplicitlyEnabledForTheConfiguredAdmin() {
        AccountEntity existingAdmin = new AccountEntity(
            "Administrator",
            "admin@example.com",
            passwordHashService.hash("OldPass1!"),
            "admin"
        );
        when(accountRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(existingAdmin));

        initializer("admin@example.com", "AdminPass1!", true).run(null);

        assertTrue(passwordHashService.matches("AdminPass1!", existingAdmin.getPasswordHash()));
        verify(accountRepository).save(existingAdmin);
    }

    @Test
    void leavesExistingAdminPasswordUnchangedWithoutExplicitReset() {
        String oldPasswordHash = passwordHashService.hash("OldPass1!");
        AccountEntity existingAdmin = new AccountEntity(
            "Administrator",
            "admin@example.com",
            oldPasswordHash,
            "admin"
        );
        when(accountRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(existingAdmin));

        initializer("admin@example.com", "AdminPass1!").run(null);

        assertEquals(oldPasswordHash, existingAdmin.getPasswordHash());
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    void rejectsPartialBootstrapCredentials() {
        AdminAccountInitializer initializer = initializer("admin@example.com", "");

        assertThrows(IllegalStateException.class, () -> initializer.run(null));

        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    private AdminAccountInitializer initializer(String email, String password) {
        return initializer(email, password, false);
    }

    private AdminAccountInitializer initializer(String email, String password, boolean resetPassword) {
        return new AdminAccountInitializer(
            accountRepository,
            passwordHashService,
            "Administrator",
            email,
            password,
            resetPassword
        );
    }
}
