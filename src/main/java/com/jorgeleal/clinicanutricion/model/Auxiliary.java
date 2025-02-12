package com.jorgeleal.clinicanutricion.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auxiliary")
@Getter
@Setter
@NoArgsConstructor
public class Auxiliary extends User {
    public Auxiliary(String idUser, String name, String surname, Date birthDate, String phone, String dni, Gender gender, UserType userType) {
        super(idUser, name, surname, birthDate, phone, dni, gender, userType);
    }
}

