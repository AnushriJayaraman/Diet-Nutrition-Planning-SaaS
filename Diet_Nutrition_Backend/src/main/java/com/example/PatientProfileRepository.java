package com.example;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientProfileRepository extends JpaRepository<PatientProfileEntity, Long> {
    Optional<PatientProfileEntity> findByEmail(String email);
}
