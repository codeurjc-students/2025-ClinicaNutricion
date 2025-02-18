package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auxiliary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auxiliary {
    @Id
    private String idUser;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user")
    private User user;
}

