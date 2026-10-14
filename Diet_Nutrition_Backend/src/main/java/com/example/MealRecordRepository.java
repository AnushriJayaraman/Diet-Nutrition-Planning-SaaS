package com.example;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRecordRepository extends JpaRepository<MealRecordEntity, Long> {
    List<MealRecordEntity> findAllByTrackingIdInOrderByIdAsc(Collection<Long> trackingIds);
}
