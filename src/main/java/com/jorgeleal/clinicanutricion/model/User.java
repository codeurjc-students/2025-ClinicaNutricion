package com.jorgeleal.clinicanutricion.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    @Column(name = "id_user")
    private String idUser;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "birth_date", nullable = false)
    private Date birthDate;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "mail", nullable = false)
    private String mail;

    @Column(name = "dni", unique = true, nullable = false)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private Nutritionist nutritionist;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private Auxiliary auxiliary;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private AdminAuxiliary adminAuxiliary;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private Patient patient;
}
