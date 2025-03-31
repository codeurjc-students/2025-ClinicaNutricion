package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    Optional<Patient> findByUserIdUser(Long idUser);
    @Query("SELECT n FROM Patient n WHERE " +
        "(:name IS NULL OR LOWER(n.user.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
        "AND (:surname IS NULL OR LOWER(n.user.surname) LIKE LOWER(CONCAT('%', :surname, '%'))) " +
        "AND (:phone IS NULL OR n.user.phone LIKE CONCAT('%', :phone, '%')) " +
        "AND (:email IS NULL OR LOWER(n.user.mail) LIKE LOWER(CONCAT('%', :email, '%'))) " +
        "AND (:active IS NULL OR n.active = :active)")
    List<Patient> findByUserFilters(@Param("name") String name, 
                            @Param("surname") String surname, 
                            @Param("phone") String phone, 
                            @Param("email") String email,
                            @Param("active") Boolean active);
}
