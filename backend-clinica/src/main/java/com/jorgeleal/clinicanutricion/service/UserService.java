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

    public User getUserByIdUser(Long idUser) {
        return userRepository.findByIdUser(idUser).orElse(null);
    }

    public User getUserByCognitoId(String cognitoId) {
        return userRepository.findByCognitoId(cognitoId);
    }

    public User updateUser(User updatedUser) {
        User existingUser = userRepository.findByIdUser(updatedUser.getIdUser()).orElse(null);

        if (existingUser == null) {
            throw new RuntimeException("El usuario con ID " + updatedUser.getIdUser() + " no existe.");
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setSurname(updatedUser.getSurname());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        existingUser.setMail(updatedUser.getMail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setGender(updatedUser.getGender());
        
        return userRepository.save(existingUser);
    }

}
