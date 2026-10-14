package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionTrackingRepository extends JpaRepository<NutritionTrackingEntity, Long> {
    java.util.List<NutritionTrackingEntity> findAllByPatientEmail(String patientEmail);
    java.util.List<NutritionTrackingEntity> findAllByNutritionistEmail(String nutritionistEmail);
    java.util.List<NutritionTrackingEntity> findAllByPatientEmailOrderByTrackedDateDesc(String patientEmail);
    java.util.List<NutritionTrackingEntity> findAllByNutritionistEmailOrderByTrackedDateDesc(String nutritionistEmail);
    boolean existsByPatientEmailAndTrackedDate(String patientEmail, java.time.LocalDate trackedDate);
}
