package com.jorgeleal.clinicanutricion.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jorgeleal.clinicanutricion.dto.AdminAuxiliaryDTO;
import com.jorgeleal.clinicanutricion.model.AdminAuxiliary;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.repository.AdminAuxiliaryRepository;


@Service
public class AdminAuxiliaryService {
    @Autowired
    private AdminAuxiliaryRepository adminAuxiliaryRepository;

    @Autowired
    private UserService userService;

    public AdminAuxiliary updateAdminAuxiliary(Long id, AdminAuxiliaryDTO dto) {
        AdminAuxiliary admin = adminAuxiliaryRepository.findByUserIdUser(id).orElse(null);
        if (admin == null) {
            throw new RuntimeException("El AdminAuxiliary con ID " + id + " no existe.");
        }
    
        User updatedUser = admin.getUser();
        updatedUser.setName(dto.getName());
        updatedUser.setSurname(dto.getSurname());
        updatedUser.setBirthDate(dto.getBirthDate());
        updatedUser.setPhone(dto.getPhone());
        updatedUser.setGender(dto.getGender());
    
        userService.updateUser(updatedUser);
    
        return adminAuxiliaryRepository.save(admin);
    }
}
