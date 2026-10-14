package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient_profiles")
@Getter
@Setter
@NoArgsConstructor
public class PatientProfileEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, unique = true, length = 254) private String email;
    @Column(nullable = false, length = 40) private String phone;
    @Column(nullable = false, length = 20) private String dateOfBirth;
    @Column(nullable = false, length = 24) private String gender;
    @Column private Double heightCm;
    @Column private Double currentWeightKg;
    @Column(columnDefinition = "TEXT") private String disease;
    @Column(columnDefinition = "TEXT") private String medicalConditions;
    @Column(columnDefinition = "TEXT") private String allergies;
    @Column(columnDefinition = "TEXT") private String foodPreferences;
    @Column(columnDefinition = "TEXT") private String dietaryRestrictions;
    @Column(length = 24) private String foodPreference;
    @Column private Double weightGoalKg;
}
