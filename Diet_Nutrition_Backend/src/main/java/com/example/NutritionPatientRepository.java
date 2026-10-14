package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NutritionPatientRepository extends JpaRepository<NutritionPatientEntity, Long> {
    List<NutritionPatientEntity> findAllByNutritionistEmail(String nutritionistEmail);
    Optional<NutritionPatientEntity> findFirstByEmail(String email);
    boolean existsByEmailAndNutritionistEmail(String email, String nutritionistEmail);
}
