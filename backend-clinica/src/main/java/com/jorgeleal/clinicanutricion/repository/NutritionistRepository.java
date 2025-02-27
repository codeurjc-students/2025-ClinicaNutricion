package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Nutritionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface NutritionistRepository extends JpaRepository<Nutritionist, String> {
    List<Nutritionist> findByUserNameContainingIgnoreCase(String name);
    long countByIdUser(String idUser);


    //Lower hace que la busqueda sea insensible a mayusculas y minusculas
    @Query("SELECT n FROM Nutritionist n WHERE " +
            "(:name IS NULL OR LOWER(n.user.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(n.user.surname) LIKE LOWER(CONCAT('%', :surname, '%'))) " +
            "AND (:phone IS NULL OR n.user.phone LIKE CONCAT('%', :phone, '%')) " +
            "AND (:email IS NULL OR LOWER(n.user.mail) LIKE LOWER(CONCAT('%', :email, '%')))")
    List<Nutritionist> findByUserFilters(@Param("name") String name, 
                                         @Param("surname") String surname, 
                                         @Param("phone") String phone, 
                                         @Param("email") String email);
}


