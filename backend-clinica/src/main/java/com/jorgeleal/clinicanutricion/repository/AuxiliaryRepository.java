package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Auxiliary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuxiliaryRepository extends JpaRepository<Auxiliary, String> {
}
