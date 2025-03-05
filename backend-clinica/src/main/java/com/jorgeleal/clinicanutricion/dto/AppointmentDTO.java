package com.jorgeleal.clinicanutricion.dto;

import com.jorgeleal.clinicanutricion.model.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class AppointmentDTO {
    private String id;
    private LocalDate date;
    private String time;
    private int duration;
    private String patientName;
    private String nutritionistId;
    private String patientId;

    public AppointmentDTO(String id, LocalDate date, String time, int duration, String patientName) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.patientName = patientName;
    }

    public AppointmentDTO(Appointment appointment) {
        this.id = appointment.getIdAppointment();
        this.date = appointment.getDate();
        this.time = appointment.getTime().toString();
        this.nutritionistId = appointment.getNutritionist().getIdUser();
        this.patientId = appointment.getPatient().getIdUser();
    }
}
