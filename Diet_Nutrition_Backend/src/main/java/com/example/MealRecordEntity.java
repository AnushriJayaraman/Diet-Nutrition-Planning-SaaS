package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meal_records")
@Getter
@Setter
@NoArgsConstructor
public class MealRecordEntity extends BaseEntity {
    @Column(nullable = false)
    private Long trackingId;

    @Column(nullable = false, length = 24)
    private String mealType;

    @Column(nullable = false, length = 160)
    private String foodName;

    @Column(nullable = false, length = 80)
    private String portion;

    @Column(nullable = false)
    private Double calories;

    @Column(nullable = false)
    private Double proteinGrams;

    @Column(nullable = false)
    private Double carbohydratesGrams;

    @Column(nullable = false)
    private Double fatGrams;
}
