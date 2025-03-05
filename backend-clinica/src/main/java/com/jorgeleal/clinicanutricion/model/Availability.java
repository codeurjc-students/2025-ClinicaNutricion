package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "availability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_availability")
    private String idAvailability;

    @ManyToOne
    @JoinColumn(name = "id_nutritionist")
    private Nutritionist nutritionist;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime start_time;

    @Column(name = "number_of_appointments", nullable = false)
    private int number_of_appointments;

    @Column(name = "reason")
    private String reason;
}
