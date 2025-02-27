package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_appointment")
    private String idAppointment;

    @ManyToOne
    @JoinColumn(name = "id_nutritionist")
    private Nutritionist nutritionist;

    @ManyToOne
    @JoinColumn(name = "id_patient")
    private Patient patient;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time", nullable = false)
    private LocalTime time;
}
