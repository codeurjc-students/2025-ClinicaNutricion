package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nutritionist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Nutritionist extends User {

    @Column(name = "collegiate_number", unique = true, nullable = false)
    private String collegiateNumber;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "appointment_duration", nullable = false)
    private int appointmentDuration;

    @Column(name = "min_days_between_appointments", nullable = false)
    private int minDaysBetweenAppointments;

    @Column(name = "max_active_appointments", nullable = false)
    private int maxActiveAppointments;
}
