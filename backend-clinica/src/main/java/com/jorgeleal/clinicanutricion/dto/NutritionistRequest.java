package com.jorgeleal.clinicanutricion.dto; 

import com.jorgeleal.clinicanutricion.model.Gender; 
import lombok.Getter; 
import lombok.Setter; 
import java.time.LocalDate; 
import java.time.LocalTime; 

@Getter 
@Setter 
public class NutritionistRequest {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String dni;
    private String email;
    private String phone;
    private Gender gender;
    private int appointmentDuration;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxActiveAppointments;
    private int minDaysBetweenAppointments;
}
