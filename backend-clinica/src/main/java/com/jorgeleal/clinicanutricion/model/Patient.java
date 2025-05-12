package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id
    @Column(name = "id_user")
    private Long idUser;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user", referencedColumnName = "idUser")
    private User user;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
