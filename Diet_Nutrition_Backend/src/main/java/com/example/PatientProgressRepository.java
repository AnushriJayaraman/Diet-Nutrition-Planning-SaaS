package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientProgressRepository extends JpaRepository<PatientProgressEntity, Long> {
    java.util.List<PatientProgressEntity> findAllByEmail(String email);
}
