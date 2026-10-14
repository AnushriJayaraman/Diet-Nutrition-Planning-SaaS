package com.example;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NutritionistAvailabilityRepository extends JpaRepository<NutritionistAvailabilityEntity, Long> {
    List<NutritionistAvailabilityEntity> findAllByNutritionistEmailOrderByDayOfWeekAsc(String nutritionistEmail);
    java.util.Optional<NutritionistAvailabilityEntity> findByNutritionistEmailAndDayOfWeek(
        String nutritionistEmail,
        Integer dayOfWeek
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select availability from NutritionistAvailabilityEntity availability "
        + "where availability.nutritionistEmail = :email and availability.dayOfWeek = :day")
    Optional<NutritionistAvailabilityEntity> findForUpdateByNutritionistAndDay(
        @Param("email") String nutritionistEmail,
        @Param("day") Integer dayOfWeek
    );
    void deleteAllByNutritionistEmail(String nutritionistEmail);
}
