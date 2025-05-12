package com.jorgeleal.clinicanutricion.dto; 

import lombok.Getter; 
import lombok.Setter; 
import java.time.LocalDate; 

@Getter 
@Setter 
public class UserDTO {
    private String cognitoId;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String phone;
    private String mail;
    private String gender;
    private String userType;
}