package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionistRepository extends JpaRepository<NutritionistEntity, Long> {
    boolean existsByEmail(String email);
    java.util.Optional<NutritionistEntity> findByEmail(String email);
}
