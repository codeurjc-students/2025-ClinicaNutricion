package com.jorgeleal.clinicanutricion.dto;

import com.jorgeleal.clinicanutricion.model.Appointment;
import com.jorgeleal.clinicanutricion.model.AppointmentType;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class AppointmentDTO {
    private String idAppointment;
    private Long idNutritionist;
    private Long idPatient;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentType type;
    private PatientDTO patient;
}
