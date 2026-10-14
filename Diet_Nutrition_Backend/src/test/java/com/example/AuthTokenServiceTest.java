package com.example;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AuthTokenServiceTest {

    @Test
    void rejectsMissingOrShortTokenSigningSecret() {
        assertThrows(IllegalStateException.class, () -> new AuthTokenService(""));
        assertThrows(IllegalStateException.class, () -> new AuthTokenService("too-short"));
    }
}
