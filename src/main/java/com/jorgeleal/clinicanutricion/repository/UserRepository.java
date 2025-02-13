package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
