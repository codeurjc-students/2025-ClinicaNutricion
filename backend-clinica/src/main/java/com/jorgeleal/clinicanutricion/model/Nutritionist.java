package com.jorgeleal.clinicanutricion.model;

import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
@Table(name = "nutritionist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Nutritionist {
    @Id
    @Column(name = "id_user")
    private Long idUser;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user", referencedColumnName = "idUser")
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

    @Column(name = "active", columnDefinition = "TINYINT(1)", nullable = false)
    private boolean active = true;

    public int getAppointmentDuration() {
        return appointmentDuration;
    }
}
