package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id
    private String idUser;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user")
    private User user;
}
