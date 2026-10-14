package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient_bmi_assessments")
@Getter
@Setter
@NoArgsConstructor
public class PatientBmiEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String patientName;
    @Column(length = 254) private String email;
    @Column(nullable = false) private Double heightCm;
    @Column(nullable = false) private Double weightKg;
    @Column(nullable = false) private Double bmi;
}
