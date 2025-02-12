package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor

public class Patient extends User {
    public Patient(String idUser, String name, String surname, Date birthDate, String phone, String dni, Gender gender, UserType userType) {
        super(idUser, name, surname, birthDate, phone, dni, gender, userType);
    }
}
