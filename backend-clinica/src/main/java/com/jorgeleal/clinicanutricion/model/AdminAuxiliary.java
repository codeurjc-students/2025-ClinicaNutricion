package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
@Table(name = "admin_auxiliary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuxiliary {
    @Id
    private Long idUser;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user")
    private User user;
}
