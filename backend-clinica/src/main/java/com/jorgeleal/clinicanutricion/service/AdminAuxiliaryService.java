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
    

    public ResponseEntity<?> getUserById(String userType, String id) {
        switch (userType.toLowerCase()) {
            case "nutritionist":
                Nutritionist nutritionist = nutritionistService.getNutritionistById(id);
                if (nutritionist == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nutricionista no encontrado");
                }
                return ResponseEntity.ok(nutritionist);
    
            case "patient":
                Patient patient = patientService.getPatientById(id);
                if (patient == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente no encontrado");
                }
                return ResponseEntity.ok(patient);
    
            case "auxiliary":
                Auxiliary auxiliary = auxiliaryService.getAuxiliaryById(id);
                if (auxiliary == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auxiliar no encontrado");
                }
                return ResponseEntity.ok(auxiliary);
    
            case "admin_auxiliary":
                AdminAuxiliary adminAuxiliary = getAdminAuxiliaryById(id);
                if (adminAuxiliary == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrador auxiliar no encontrado");
                }
                return ResponseEntity.ok(adminAuxiliary);
    
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tipo de usuario no válido");
        }
    }

    public AdminAuxiliary updateAdminAuxiliary(String id, AdminAuxiliaryDTO dto) {
        AdminAuxiliary admin = adminAuxiliaryRepository.findById(id).orElse(null);
    
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

    public AdminAuxiliary getAdminAuxiliaryById(String id) {
        AdminAuxiliary admin = adminAuxiliaryRepository.findById(id).orElse(null);
        return admin;
    }
    
}
