package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient_nutrition_tracking")
@Getter
@Setter
@NoArgsConstructor
public class PatientNutritionTrackingEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String patientName;
    @Column(length = 254) private String email;
    @Column(nullable = false) private Double waterLitres;
    @Column(nullable = false) private Integer mealsCompleted;
    @Column(columnDefinition = "TEXT") private String notes;
}
