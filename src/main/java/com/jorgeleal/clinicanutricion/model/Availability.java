package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

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
    private Date date;

    @Column(name = "reason")
    private String reason;
}
