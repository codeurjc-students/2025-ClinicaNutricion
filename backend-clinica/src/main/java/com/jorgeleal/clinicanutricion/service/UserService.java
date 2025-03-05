package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User getUserByIdUser(String idUser) {
        return userRepository.findByIdUser(idUser)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUser));
    }
}
