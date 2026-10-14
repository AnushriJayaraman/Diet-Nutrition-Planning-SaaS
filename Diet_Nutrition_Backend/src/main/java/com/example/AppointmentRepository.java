package com.example;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findAllByPatientEmailOrderByStartsAtAsc(String patientEmail);
    List<AppointmentEntity> findAllByNutritionistEmailOrderByStartsAtAsc(String nutritionistEmail);
    List<AppointmentEntity> findAllByPatientEmailAndStartsAt(String patientEmail, LocalDateTime startsAt);
    List<AppointmentEntity> findAllByNutritionistEmailAndStartsAt(String nutritionistEmail, LocalDateTime startsAt);
}
