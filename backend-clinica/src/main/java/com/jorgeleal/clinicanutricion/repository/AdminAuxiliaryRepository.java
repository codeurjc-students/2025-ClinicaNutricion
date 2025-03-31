package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.AdminAuxiliary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdminAuxiliaryRepository extends JpaRepository<AdminAuxiliary, String> {
    Optional<AdminAuxiliary> findByUserIdUser(Long idUser);
}
