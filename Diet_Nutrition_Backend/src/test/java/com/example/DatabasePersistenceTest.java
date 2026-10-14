package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class DatabasePersistenceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Test
    void repositoriesPersistAccountsAndPatientProfiles() {
        AccountEntity account = accountRepository.save(
            new AccountEntity("Casey Morgan", "casey@example.com", "hash", "patient")
        );
        PatientProfileEntity profile = new PatientProfileEntity();
        profile.setName("Casey Morgan");
        profile.setEmail("casey@example.com");
        profile.setPhone("+14155552671");
        profile.setDateOfBirth("1990-01-01");
        profile.setGender("Other");
        patientProfileRepository.save(profile);

        assertEquals(account.getId(), accountRepository.findByEmail("casey@example.com").orElseThrow().getId());
        assertEquals("Casey Morgan", patientProfileRepository.findByEmail("casey@example.com").orElseThrow().getName());
    }
}
