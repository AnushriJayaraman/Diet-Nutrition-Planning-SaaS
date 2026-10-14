package com.example;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nutritionists")
@Getter
@Setter
@NoArgsConstructor
public class NutritionistEntity extends BaseEntity {
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 254) private String email;
    @Column(nullable = false, length = 160) private String specialization;
    @Column(length = 40) private String phone;
    @Column(length = 160) private String qualification;
    @Column private Integer experienceYears;
    @Column(columnDefinition = "TEXT") private String bio;
}
