package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByPatientIdUser(String patientId);
    List<Appointment> findByNutritionistIdUser(String nutritionistId);
    List<Appointment> findByNutritionist_IdUserAndDate(String nutritionistId, LocalDate date);
    List<Appointment> findByDate(LocalDate date);
    
    boolean existsByNutritionistIdUserAndDateAndTime(String nutritionistId, LocalDate date, LocalTime time);
}
