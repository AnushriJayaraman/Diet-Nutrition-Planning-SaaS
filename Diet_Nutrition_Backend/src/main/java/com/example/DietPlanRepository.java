package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DietPlanRepository extends JpaRepository<DietPlanEntity, Long> {
    java.util.List<DietPlanEntity> findAllByPatientEmail(String patientEmail);
    java.util.List<DietPlanEntity> findAllByNutritionistEmail(String nutritionistEmail);
}
