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
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
public class AppointmentEntity extends BaseEntity {
    @Column(nullable = false, length = 254)
    private String patientEmail;

    @Column(nullable = false, length = 120)
    private String patientName;

    @Column(nullable = false, length = 254)
    private String nutritionistEmail;

    @Column(nullable = false, length = 120)
    private String nutritionistName;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false, length = 24)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String patientNote;

    @Column(columnDefinition = "TEXT")
    private String consultationNote;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void initializeAppointment() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "Requested";
        }
    }
}
