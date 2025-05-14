package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.NutritionistRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalTime;

@Service
public class NutritionistService {

    @Autowired
    private NutritionistRepository nutritionistRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CognitoService cognitoService;

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
        dto.setActive(nutritionist.isActive());
    
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
        nutritionist.setActive(dto.isActive());
    
        return nutritionist;
    }

    private UserDTO convertToUserDTO(NutritionistDTO dto) {
        UserDTO userDTO = new UserDTO();

        userDTO.setName(dto.getName());
        userDTO.setSurname(dto.getSurname());
        userDTO.setBirthDate(dto.getBirthDate());
        userDTO.setMail(dto.getMail());
        userDTO.setPhone(dto.getPhone());
        userDTO.setGender(dto.getGender().toString());
        userDTO.setUserType("nutritionist");

        return userDTO;
    }
    
    
    public NutritionistDTO createNutritionist(NutritionistDTO dto) {
        if (userService.mailExists(dto.getMail())) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }
        Nutritionist nutritionist = convertToDomain(dto);
        String idCognito = cognitoService.createCognitoUser(convertToUserDTO(dto));
        nutritionist.setActive(true);
        User user = nutritionist.getUser();
        user.setCognitoId(idCognito);
        nutritionist.setUser(userService.saveUser(user));
        return convertToDTO(nutritionistRepository.save(nutritionist));
    }

    public Nutritionist getNutritionistById(Long id) {
        Nutritionist nutritionist = nutritionistRepository.findByUserIdUser(id)
            .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
        return nutritionist;
    }

    public NutritionistDTO getNutritionistByIdDTO(Long id) {
        Nutritionist nutritionist = getNutritionistById(id);
        return convertToDTO(nutritionist);
    }

    public NutritionistDTO updateNutritionist(Long id, NutritionistDTO dto) {
        Nutritionist existingNutritionist = nutritionistRepository.findByUserIdUser(id).orElse(null);
        if (existingNutritionist == null) {
            throw new RuntimeException("Nutricionista no encontrado");
        }
    
        User updatedUser = existingNutritionist.getUser();
        updatedUser.setName(dto.getName());
        updatedUser.setSurname(dto.getSurname());
        updatedUser.setBirthDate(dto.getBirthDate());
        updatedUser.setPhone(dto.getPhone());
        updatedUser.setGender(dto.getGender());
    
        userService.updateUser(updatedUser);
        cognitoService.updateCognitoUser(convertToUserDTO(dto));
    
        existingNutritionist.setAppointmentDuration(dto.getAppointmentDuration());
        existingNutritionist.setStartTime(dto.getStartTime());
        existingNutritionist.setEndTime(dto.getEndTime());
        existingNutritionist.setMaxActiveAppointments(dto.getMaxActiveAppointments());
    
        Nutritionist savedNutritionist = nutritionistRepository.save(existingNutritionist);
        return convertToDTO(savedNutritionist);
    }
    
    public List<NutritionistDTO> getNutritionistsByFilters(String name, String surname, String fullName, String phone, String email, Boolean active) {
        List<Nutritionist> nutritionists;
    
        if (fullName != null && !fullName.trim().isEmpty()) {
            nutritionists = nutritionistRepository.findByFullName(fullName);
        } else { 
            nutritionists = nutritionistRepository.findByUserFilters(name, surname, phone, email, active);
        }
    
        return nutritionists.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changeNutritionistStatus(Long id, boolean status) {
        Nutritionist nutritionist = nutritionistRepository.findByUserIdUser(id).orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
        nutritionist.setActive(status);
        nutritionistRepository.save(nutritionist);

        String username = nutritionist.getUser().getMail();  
        if (status) {
            cognitoService.enableUser(username);
        } else {
            cognitoService.disableUser(username);
            cognitoService.globalSignOut(username);
        }
    }

    @Transactional
    public void deleteNutritionist(Long id) {
        Nutritionist nutritionist = nutritionistRepository.findByUserIdUser(id).orElse(null);
        if (nutritionist == null) {
            throw new RuntimeException("El Nutricionista con ID " + id + " no existe.");
        }
        cognitoService.deleteCognitoUser(nutritionist.getUser().getMail());
        nutritionistRepository.delete(nutritionist);
        userService.deleteUser(id);
    }

    public List<NutritionistDTO> getNutritionistsByTimeRange(String timeRange) {
        System.out.println("timeRange recibido: " + timeRange); 
        List<Nutritionist> nutritionists;
    
        if ("a cualquier hora".equals(timeRange)) {
            nutritionists = nutritionistRepository.findAll(); 
        } else {
            LocalTime startHour = LocalTime.of(0, 0);
            LocalTime endHour = LocalTime.of(23, 59);
    
            switch (timeRange) {
                case "mañana":
                    startHour = LocalTime.of(9, 0);
                    endHour = LocalTime.of(12, 0);
                    break;
                case "mediodía":
                    startHour = LocalTime.of(12, 0);
                    endHour = LocalTime.of(16, 0);
                    break;
                case "tarde":
                    startHour = LocalTime.of(16, 0);
                    endHour = LocalTime.of(20, 0);
                    break;
                default:
                    throw new IllegalArgumentException("Franja horaria no válida");
            }
    
            nutritionists = nutritionistRepository.findByAvailableTimeRange(startHour, endHour);
        }
    
        return nutritionists.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
