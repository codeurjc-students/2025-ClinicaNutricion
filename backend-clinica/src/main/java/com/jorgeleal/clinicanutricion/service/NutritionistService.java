package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.service.*;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.NutritionistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NutritionistService {

    @Autowired
    private NutritionistRepository nutritionistRepository;

    @Autowired
    private UserService userService;

    private NutritionistDTO convertToDTO(Nutritionist nutritionist) {
        NutritionistDTO dto = new NutritionistDTO();
        User user = nutritionist.getUser();
    
        dto.setIdUser(nutritionist.getIdUser());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setBirthDate(user.getBirthDate());
        dto.setMail(user.getMail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setAppointmentDuration(nutritionist.getAppointmentDuration());
        dto.setStartTime(nutritionist.getStartTime());
        dto.setEndTime(nutritionist.getEndTime());
        dto.setMaxActiveAppointments(nutritionist.getMaxActiveAppointments());
        dto.setMinDaysBetweenAppointments(nutritionist.getMinDaysBetweenAppointments());
    
        return dto;
    }

    private Nutritionist convertToDomain(NutritionistDTO dto) {
        Nutritionist nutritionist = new Nutritionist();
        User user = new User();
    
        user.setIdUser(dto.getIdUser());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setMail(dto.getMail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setUserType(UserType.NUTRITIONIST);
    
        nutritionist.setUser(user);
        nutritionist.setAppointmentDuration(dto.getAppointmentDuration());
        nutritionist.setStartTime(dto.getStartTime());
        nutritionist.setEndTime(dto.getEndTime());
        nutritionist.setMaxActiveAppointments(dto.getMaxActiveAppointments());
        nutritionist.setMinDaysBetweenAppointments(dto.getMinDaysBetweenAppointments());
    
        return nutritionist;
    }
    
    
    public Nutritionist createNutritionist(NutritionistDTO dto) {
        Nutritionist nutritionist = convertToDomain(dto);
        userService.saveUser(nutritionist.getUser());
        return nutritionistRepository.save(nutritionist);
    }

    public Nutritionist getNutritionistById(String id) {
        return nutritionistRepository.findById(id).orElse(null);
    }

    public Nutritionist updateNutritionist(String id, NutritionistDTO dto) {
        Nutritionist nutritionist = nutritionistRepository.findById(id).orElse(null);
        nutritionist = convertToDomain(dto);
        nutritionist.setIdUser(id);
        userService.saveUser(nutritionist.getUser());
        return nutritionistRepository.save(nutritionist);
    }

    public List<Nutritionist> getNutritionistsByFilters(String name, String surname, String fullName, String phone, String email) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return nutritionistRepository.findByFullName(fullName);
        }
        return nutritionistRepository.findByUserFilters(name, surname, phone, email);
    }

    public void deleteNutritionist(String id) {
        nutritionistRepository.deleteById(id);
    }
}
