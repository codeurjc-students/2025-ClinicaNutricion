package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByPatientIdUser(String patientId);
    List<Appointment> findByNutritionistIdUser(String nutritionistId);
    
    List<Appointment> findByNutritionist_IdUserOrderByDateAscStartTimeAsc(String idUser);
    boolean existsByNutritionistIdUserAndDateAndStartTime(String nutritionistId, LocalDate date, LocalTime startTime);

    @Query("SELECT a FROM Appointment a WHERE a.nutritionist.idUser = :nutritionistId " +
    "AND a.date = :date " +
    "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
        List<Appointment> findConflictAppointments(
        @Param("nutritionistId") String nutritionistId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
        );


    @Query("SELECT a FROM Appointment a WHERE a.nutritionist.idUser = :nutritionistId " +
    "AND a.date = :date ORDER BY a.startTime ASC")
        List<Appointment> findByNutritionist_IdUserAndDateOrderByStartTimeAsc(
                @Param("nutritionistId") String nutritionistId,
                @Param("date") LocalDate date
        );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.nutritionist.id = :nutritionistId " +
    "AND a.date = :date " +
    "AND a.id <> :appointmentId " + 
    "AND ((a.startTime < :endTime AND a.endTime > :startTime))") 
        boolean existsByNutritionistIdAndDateAndTimeRange(
                @Param("nutritionistId") String nutritionistId,
                @Param("date") LocalDate  date,
                @Param("startTime") LocalTime startTime,
                @Param("endTime") LocalTime endTime,
                @Param("appointmentId") String appointmentId
        );
}
