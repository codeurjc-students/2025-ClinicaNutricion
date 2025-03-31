package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByIdUser(Long idUser);
    User findByCognitoId(String cognitoId);
}
