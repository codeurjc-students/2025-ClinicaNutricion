package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Nutritionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalTime;

@Repository
public interface NutritionistRepository extends JpaRepository<Nutritionist, String> {
        List<Nutritionist> findByUserNameContainingIgnoreCase(String name);
        long countByIdUser(String idUser);
        
        @Query("SELECT n FROM Nutritionist n WHERE " +
                "(:name IS NULL OR LOWER(n.user.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
                "AND (:surname IS NULL OR LOWER(n.user.surname) LIKE LOWER(CONCAT('%', :surname, '%'))) " +
                "AND (:phone IS NULL OR n.user.phone LIKE CONCAT('%', :phone, '%')) " +
                "AND (:email IS NULL OR LOWER(n.user.mail) LIKE LOWER(CONCAT('%', :email, '%'))) " +
                "AND (:active IS NULL OR n.active = :active)")
        List<Nutritionist> findByUserFilters(@Param("name") String name, 
                                     @Param("surname") String surname, 
                                     @Param("phone") String phone, 
                                     @Param("email") String email,
                                     @Param("active") Boolean active);


        @Query("SELECT n FROM Nutritionist n JOIN n.user u " + 
                "WHERE n.active = true " +
                "AND LOWER(CONCAT(u.name, ' ', u.surname)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
        List<Nutritionist> findByFullName(@Param("fullName") String fullName);

        @Query("SELECT n FROM Nutritionist n WHERE " +
                "(n.startTime < :endHour AND n.endTime > :startHour)")
        List<Nutritionist> findByAvailableTimeRange(@Param("startHour") LocalTime startHour, @Param("endHour") LocalTime endHour);
}



