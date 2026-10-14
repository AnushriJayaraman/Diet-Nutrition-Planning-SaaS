package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientBmiRepository extends JpaRepository<PatientBmiEntity, Long> {
    java.util.List<PatientBmiEntity> findAllByEmail(String email);
}
