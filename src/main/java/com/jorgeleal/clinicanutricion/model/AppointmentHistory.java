package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "appointment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_appointment_history")
    private String idAppointmentHistory;

    @ManyToOne
    @JoinColumn(name = "id_patient")
    private Patient patient;

    @Column(name = "details")
    private String details;
}
