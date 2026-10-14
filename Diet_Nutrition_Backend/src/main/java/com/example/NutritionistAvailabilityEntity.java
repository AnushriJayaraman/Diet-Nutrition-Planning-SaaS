package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "nutritionist_availability",
    uniqueConstraints = @UniqueConstraint(columnNames = {"nutritionistEmail", "dayOfWeek"})
)
@Getter
@Setter
@NoArgsConstructor
public class NutritionistAvailabilityEntity extends BaseEntity {
    @Column(nullable = false, length = 254)
    private String nutritionistEmail;

    @Column(nullable = false)
    private Integer dayOfWeek;

    @Column(nullable = false, length = 5)
    private String startTime;

    @Column(nullable = false, length = 5)
    private String endTime;

    @Column(nullable = false)
    private boolean active = true;
}
