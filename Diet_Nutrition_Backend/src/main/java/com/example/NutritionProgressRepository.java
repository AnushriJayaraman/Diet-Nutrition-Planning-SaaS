package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionProgressRepository extends JpaRepository<NutritionProgressEntity, Long> {
    java.util.List<NutritionProgressEntity> findAllByPatientEmail(String patientEmail);
    java.util.List<NutritionProgressEntity> findAllByNutritionistEmail(String nutritionistEmail);
    java.util.List<NutritionProgressEntity> findAllByPatientEmailOrderByRecordedAtDesc(String patientEmail);
    java.util.List<NutritionProgressEntity> findAllByNutritionistEmailOrderByRecordedAtDesc(String nutritionistEmail);
}
