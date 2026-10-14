package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bmi_assessments")
@Getter
@Setter
@NoArgsConstructor
public class BmiAssessmentEntity extends BaseEntity {
    @Column(length = 120) private String patientName;
    @Column(length = 254) private String patientEmail;
    @Column(length = 254) private String nutritionistEmail;
    @Column(nullable = false) private Double heightCm;
    @Column(nullable = false) private Double weightKg;
    @Column(nullable = false) private Double bmi;
    @Column(name = "assessed_at")
    private LocalDateTime assessedAt;

    @PrePersist
    void setAssessmentTimestamp() {
        if (assessedAt == null) {
            assessedAt = LocalDateTime.now();
        }
    }
}
