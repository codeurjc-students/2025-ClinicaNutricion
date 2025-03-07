package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.service.*;
import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.AuxiliaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Auxiliary> getAllAuxiliaries() {
        return auxiliaryRepository.findAll();
    }

    public List<Auxiliary> getAuxiliariesByFilters(String name, String surname, String phone, String email) {
        return auxiliaryRepository.findByUserFilters(name, surname, phone, email);
    }

    public Auxiliary getAuxiliaryById(String id) {
        return auxiliaryRepository.findById(id).orElse(null);
    }

    public Auxiliary createAuxiliary(AuxiliaryDTO dto) {
        Auxiliary auxiliary = convertToDomain(dto);
        auxiliary.setUser(userService.saveUser(auxiliary.getUser())); 
        return auxiliaryRepository.save(auxiliary);
    }

    public Auxiliary updateAuxiliary(String id, AuxiliaryDTO dto) {
        Auxiliary auxiliary = convertToDomain(dto);
        auxiliary.setIdUser(id);
        userService.saveUser(auxiliary.getUser());
        return auxiliaryRepository.save(auxiliary);
    }

    public void deleteAuxiliary(String id) {
        if (!auxiliaryRepository.existsById(id)) {
            throw new RuntimeException("Auxiliar no encontrado");
        }
        auxiliaryRepository.deleteById(id);
    }
}
