package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.Nutritionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NutritionistRepository extends JpaRepository<Nutritionist, String> {
}
