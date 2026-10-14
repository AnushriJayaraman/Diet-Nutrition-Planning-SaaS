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
@Table(name = "nutrition_progress")
@Getter
@Setter
@NoArgsConstructor
public class NutritionProgressEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String patientName;
    @Column(length = 254) private String patientEmail;
    @Column(length = 254) private String nutritionistEmail;
    @Column(nullable = false) private Double currentWeight;
    @Column(nullable = false) private Double targetWeight;
    @Column(nullable = false, length = 80) private String progress;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column private LocalDateTime recordedAt;

    @PrePersist
    void setRecordedAt() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
