package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.service.*;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.AuxiliaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuxiliaryService {

    @Autowired
    private AuxiliaryRepository auxiliaryRepository;

    @Autowired
    private UserService userService;

    private AuxiliaryDTO convertToDTO(Auxiliary auxiliary) {
        AuxiliaryDTO dto = new AuxiliaryDTO();
        User user = auxiliary.getUser();
    
        dto.setIdUser(user.getIdUser());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setBirthDate(user.getBirthDate());
        dto.setMail(user.getMail());
        dto.setPhone(user.getPhone());
        dto.setGender(user.getGender());
    
        return dto;
    }

    private Auxiliary convertToDomain(AuxiliaryDTO dto) {
        Auxiliary auxiliary = new Auxiliary();
        User user = new User();
    
        user.setIdUser(dto.getIdUser());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setBirthDate(dto.getBirthDate());
        user.setMail(dto.getMail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setUserType(UserType.AUXILIARY);
    
        auxiliary.setUser(user);
    
        return auxiliary;
    }

    public List<AuxiliaryDTO> getAuxiliariesByFilters(String name, String surname, String phone, String email) {
        List<Auxiliary> auxiliaries = auxiliaryRepository.findByUserFilters(name, surname, phone, email);
        
        if (auxiliaries.isEmpty()) {
            throw new RuntimeException("No se encontraron auxiliares");
        }
        return auxiliaries.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public AuxiliaryDTO getAuxiliaryById(String id) {
        Auxiliary auxiliary = auxiliaryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auxiliar no encontrado"));
        return convertToDTO(auxiliary);  
    }

    public Auxiliary createAuxiliary(AuxiliaryDTO dto) {
        Auxiliary auxiliary = convertToDomain(dto);
        auxiliary.setUser(userService.saveUser(auxiliary.getUser())); 
        return auxiliaryRepository.save(auxiliary);
    }

    public Auxiliary updateAuxiliary(String id, AuxiliaryDTO dto) {
        Auxiliary existingAuxiliary = auxiliaryRepository.findById(id).orElse(null);
        if (existingAuxiliary == null) {
            throw new RuntimeException("El Auxiliar con ID " + id + " no existe.");
        }
    
        User updatedUser = existingAuxiliary.getUser();
        updatedUser.setName(dto.getName());
        updatedUser.setSurname(dto.getSurname());
        updatedUser.setBirthDate(dto.getBirthDate());
        updatedUser.setMail(dto.getMail());
        updatedUser.setPhone(dto.getPhone());
        updatedUser.setGender(dto.getGender());
    
        userService.updateUser(updatedUser);
    
        return auxiliaryRepository.save(existingAuxiliary);
    }

    public void deleteAuxiliary(String id) {
        if (!auxiliaryRepository.existsById(id)) {
            throw new RuntimeException("Auxiliar no encontrado");
        }
        auxiliaryRepository.deleteById(id);
    }
}
