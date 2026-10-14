package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patient_progress")
@Getter
@Setter
@NoArgsConstructor
public class PatientProgressEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String patientName;
    @Column(length = 254) private String email;
    @Column(nullable = false) private Double currentWeight;
    @Column(nullable = false) private Double targetWeight;
    @Column(nullable = false, length = 80) private String progress;
    @Column(columnDefinition = "TEXT") private String notes;
}
