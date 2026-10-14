package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BmiAssessmentRepository extends JpaRepository<BmiAssessmentEntity, Long> {
    java.util.List<BmiAssessmentEntity> findAllByPatientEmailOrderByAssessedAtDesc(String patientEmail);
    java.util.List<BmiAssessmentEntity> findAllByNutritionistEmailOrderByAssessedAtDesc(String nutritionistEmail);
}
