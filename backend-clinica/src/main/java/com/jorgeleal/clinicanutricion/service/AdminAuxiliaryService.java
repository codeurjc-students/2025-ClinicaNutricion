package com.jorgeleal.clinicanutricion.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.jorgeleal.clinicanutricion.service.*;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.AdminAuxiliaryRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminAuxiliaryService {

    @Autowired
    private NutritionistService nutritionistService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AuxiliaryService auxiliaryService;

    @Autowired
    private AdminAuxiliaryRepository adminAuxiliaryRepository;

    @Autowired
    private UserService userService;

    private AdminAuxiliaryDTO convertToDTO(AdminAuxiliary adminAuxiliary) {
        AdminAuxiliaryDTO dto = new AdminAuxiliaryDTO();
        User user = adminAuxiliary.getUser();
    
        dto.setIdUser(user.getIdUser());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setBirthDate(user.getBirthDate());
        dto.setMail(user.getMail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
    
        return dto;
    }

    private AdminAuxiliary convertToDomain(AdminAuxiliaryDTO dto) {
        AdminAuxiliary adminAuxiliary = new AdminAuxiliary();
        User user = new User();
    
        user.setIdUser(dto.getIdUser());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setMail(dto.getMail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setUserType(UserType.ADMIN);
    
        adminAuxiliary.setUser(user);
    
        return adminAuxiliary;
    }

    public AdminAuxiliary updateAdminAuxiliary(Long id, AdminAuxiliaryDTO dto) {
        AdminAuxiliary admin = adminAuxiliaryRepository.findByUserIdUser(id).orElse(null);
        if (admin == null) {
            throw new RuntimeException("El AdminAuxiliary con ID " + id + " no existe.");
        }
    
        User updatedUser = admin.getUser();
        updatedUser.setName(dto.getName());
        updatedUser.setSurname(dto.getSurname());
        updatedUser.setBirthDate(dto.getBirthDate());
        updatedUser.setMail(dto.getMail());
        updatedUser.setPhone(dto.getPhone());
        updatedUser.setGender(dto.getGender());
    
        userService.updateUser(updatedUser);
    
        return adminAuxiliaryRepository.save(admin);
    }

    public AdminAuxiliary getAdminAuxiliaryById(Long id) {
        return adminAuxiliaryRepository.findByUserIdUser(id)
            .orElseThrow(() -> new RuntimeException("Administrador auxiliar no encontrado"));
    }
    
}
