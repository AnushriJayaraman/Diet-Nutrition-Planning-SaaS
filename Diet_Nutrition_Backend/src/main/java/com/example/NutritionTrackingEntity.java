package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nutrition_tracking")
@Getter
@Setter
@NoArgsConstructor
public class NutritionTrackingEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String patientName;
    @Column(length = 254) private String patientEmail;
    @Column(length = 254) private String nutritionistEmail;
    @Column(nullable = false) private Double waterLitres;
    @Column(nullable = false) private Integer mealsCompleted;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column private LocalDate trackedDate;

    @PrePersist
    void setTrackedDate() {
        if (trackedDate == null) {
            trackedDate = LocalDate.now();
        }
    }
}
