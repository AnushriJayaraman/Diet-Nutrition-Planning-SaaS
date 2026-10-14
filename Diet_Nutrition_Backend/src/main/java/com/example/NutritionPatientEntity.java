package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nutrition_patients")
@Getter
@Setter
@NoArgsConstructor
public class NutritionPatientEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String patientName;
    @Column(nullable = false, length = 254) private String email;
    @Column(length = 254) private String nutritionistEmail;
    @Column(columnDefinition = "TEXT") private String notes;
}
