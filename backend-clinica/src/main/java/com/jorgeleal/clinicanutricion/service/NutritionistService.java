package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.dto.NutritionistRequest;
import com.jorgeleal.clinicanutricion.model.Nutritionist;
import com.jorgeleal.clinicanutricion.repository.NutritionistRepository;
import com.jorgeleal.clinicanutricion.model.User;
import com.jorgeleal.clinicanutricion.model.UserType;
import com.jorgeleal.clinicanutricion.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NutritionistService {

    @Autowired
    private NutritionistRepository nutritionistRepository;

    @Autowired
    private UserService userService;

    public Nutritionist createNutritionist(NutritionistRequest request) {
        // Crear el usuario primero
        User user = new User();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setBirthDate(request.getBirthDate());
        user.setMail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setUserType(UserType.NUTRITIONIST);
    
        // Guardar el usuario en la base de datos
        user = userService.saveUser(user);
    
        // Crear el nutricionista y asociarlo al usuario
        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setUser(user);
        nutritionist.setAppointmentDuration(request.getAppointmentDuration());
        nutritionist.setStartTime(request.getStartTime());
        nutritionist.setEndTime(request.getEndTime());
        nutritionist.setMaxActiveAppointments(request.getMaxActiveAppointments());
        nutritionist.setMinDaysBetweenAppointments(request.getMinDaysBetweenAppointments());
    
        return nutritionistRepository.save(nutritionist);
    }


    public Nutritionist getNutritionistById(String id) {
        return nutritionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
    }

    public NutritionistRequest getNutritionistDTOById(String id) {
        Nutritionist nutritionist = getNutritionistById(id);
        User user = nutritionist.getUser();

        NutritionistRequest dto = new NutritionistRequest();
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setBirthDate(user.getBirthDate());
        dto.setEmail(user.getMail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
        dto.setAppointmentDuration(nutritionist.getAppointmentDuration());
        dto.setStartTime(nutritionist.getStartTime());
        dto.setEndTime(nutritionist.getEndTime());
        dto.setMaxActiveAppointments(nutritionist.getMaxActiveAppointments());
        dto.setMinDaysBetweenAppointments(nutritionist.getMinDaysBetweenAppointments());
        
        return dto;
        }

        public List<Nutritionist> getNutritionistsByFilters(String name, String surname, String fullName, String phone, String email) {
            if (fullName != null && !fullName.trim().isEmpty()) {
                return nutritionistRepository.findByFullName(fullName);
            }
            return nutritionistRepository.findByUserFilters(name, surname, phone, email);
        }

        public Nutritionist updateNutritionist(String id, NutritionistRequest request) {
        Nutritionist nutritionist = getNutritionistById(id);
        User user = nutritionist.getUser();

        if (request.getName() != null) user.setName(request.getName());
        if (request.getSurname() != null) user.setSurname(request.getSurname());
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());
        if (request.getEmail() != null) user.setMail(request.getEmail());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getGender() != null) user.setGender(request.getGender());

        if (request.getAppointmentDuration() > 0) nutritionist.setAppointmentDuration(request.getAppointmentDuration());
        if (request.getStartTime() != null) nutritionist.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) nutritionist.setEndTime(request.getEndTime());
        if (request.getMaxActiveAppointments() > 0) nutritionist.setMaxActiveAppointments(request.getMaxActiveAppointments());
        if (request.getMinDaysBetweenAppointments() > 0) nutritionist.setMinDaysBetweenAppointments(request.getMinDaysBetweenAppointments());

        userService.saveUser(user);
        return nutritionistRepository.save(nutritionist);
    }

    public void deleteNutritionist(String id) {
        nutritionistRepository.deleteById(id);
    }

    public long getTotalPatientsByNutritionist(String nutritionistId) {
        return nutritionistRepository.countByIdUser(nutritionistId);
    }
}
