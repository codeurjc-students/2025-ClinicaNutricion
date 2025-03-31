package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Auxiliary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

@Repository
public interface AuxiliaryRepository extends JpaRepository<Auxiliary, String> {
    Boolean existsByIdUser(Long idUser);
    void deleteByIdUser(Long idUser);
    Optional<Auxiliary> findByUserIdUser(Long idUser);
    @Query("SELECT n FROM Auxiliary n WHERE " +
            "(:name IS NULL OR LOWER(n.user.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:surname IS NULL OR LOWER(n.user.surname) LIKE LOWER(CONCAT('%', :surname, '%'))) " +
            "AND (:phone IS NULL OR n.user.phone LIKE CONCAT('%', :phone, '%')) " +
            "AND (:email IS NULL OR LOWER(n.user.mail) LIKE LOWER(CONCAT('%', :email, '%')))")
    List<Auxiliary> findByUserFilters(@Param("name") String name, 
                                    @Param("surname") String surname, 
                                    @Param("phone") String phone, 
                                    @Param("email") String email);
}
