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
    @JoinColumn(name = "id_nutritionist", referencedColumnName = "id_user", nullable = false)
    private Nutritionist nutritionist;    

    @ManyToOne
    @JoinColumn(name = "id_patient", referencedColumnName = "id_user", nullable = true) 
    private Patient patient;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "startTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "endTime", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AppointmentType type;
}
