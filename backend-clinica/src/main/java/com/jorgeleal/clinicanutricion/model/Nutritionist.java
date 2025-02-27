package com.jorgeleal.clinicanutricion.model;

import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nutritionist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Nutritionist {
    @Id
    private String idUser;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user")
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "appointment_duration", nullable = false)
    private int appointmentDuration;

    @Column(name = "min_days_between_appointments", nullable = false)
    private int minDaysBetweenAppointments;

    @Column(name = "max_active_appointments", nullable = false)
    private int maxActiveAppointments;

    public int getAppointmentDuration() {
        return appointmentDuration;
    }
}
