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
@Table(name = "diet_plans")
@Getter
@Setter
@NoArgsConstructor
public class DietPlanEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String patientName;
    @Column(length = 254) private String patientEmail;
    @Column(length = 254) private String nutritionistEmail;
    @Column(nullable = false, length = 160) private String goal;
    @Column(nullable = false, columnDefinition = "TEXT") private String plan;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(columnDefinition = "TEXT") private String breakfast;
    @Column(columnDefinition = "TEXT") private String lunch;
    @Column(columnDefinition = "TEXT") private String dinner;
    @Column(columnDefinition = "TEXT") private String snacks;
    @Column private Integer calorieTarget;
    @Column private Double proteinTargetGrams;
    @Column private Double carbohydratesTargetGrams;
    @Column private Double fatTargetGrams;
    @Column(length = 120) private String breakfastPortion;
    @Column(length = 5) private String breakfastTime;
    @Column(length = 120) private String lunchPortion;
    @Column(length = 5) private String lunchTime;
    @Column(length = 120) private String dinnerPortion;
    @Column(length = 5) private String dinnerTime;
    @Column(length = 120) private String snacksPortion;
    @Column(length = 5) private String snacksTime;
    @Column(length = 24) private String status;
    @Column private LocalDateTime createdAt;

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "Active";
        }
    }
}
