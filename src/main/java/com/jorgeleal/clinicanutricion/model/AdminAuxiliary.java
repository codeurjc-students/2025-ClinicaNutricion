package com.jorgeleal.clinicanutricion.model;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_auxiliary")
@Getter
@Setter
@NoArgsConstructor
public class AdminAuxiliary extends User {
    public AdminAuxiliary(String idUser, String name, String surname, Date birthDate, String phone, String dni, Gender gender, UserType userType) {
        super(idUser, name, surname, birthDate, phone, dni, gender, userType);
    }
}
