package com.jorgeleal.clinicanutricion.repository;

import com.jorgeleal.clinicanutricion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdUser(Long idUser);
    boolean existsByMail(String mail);
    User findByCognitoId(String cognitoId);
}
