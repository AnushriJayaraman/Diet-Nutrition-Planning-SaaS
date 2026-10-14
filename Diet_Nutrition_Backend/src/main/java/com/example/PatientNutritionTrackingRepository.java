package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientNutritionTrackingRepository extends JpaRepository<PatientNutritionTrackingEntity, Long> {
    java.util.List<PatientNutritionTrackingEntity> findAllByEmail(String email);
}
