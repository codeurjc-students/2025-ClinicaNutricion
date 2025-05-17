package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.*;
import com.jorgeleal.clinicanutricion.dto.*;
import com.jorgeleal.clinicanutricion.repository.AuxiliaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuxiliaryService {

    @Autowired
    private AuxiliaryRepository auxiliaryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CognitoService cognitoService;

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

    private UserDTO convertToUserDTO(AuxiliaryDTO dto) {
        UserDTO userDTO = new UserDTO();

        userDTO.setName(dto.getName());
        userDTO.setSurname(dto.getSurname());
        userDTO.setBirthDate(dto.getBirthDate());
        userDTO.setMail(dto.getMail());
        userDTO.setPhone(dto.getPhone());
        userDTO.setGender(dto.getGender().toString());
        userDTO.setUserType("auxiliary");

        return userDTO;
    }

    public List<AuxiliaryDTO> getAuxiliariesByFilters(String name, String surname, String phone, String email) {
        List<Auxiliary> auxiliaries = auxiliaryRepository.findByUserFilters(name, surname, phone, email);
        
        if (auxiliaries.isEmpty()) {
            throw new RuntimeException("No se encontraron auxiliares");
        }
        return auxiliaries.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Auxiliary getAuxiliaryById(Long id) {
        Auxiliary auxiliary = auxiliaryRepository.findByUserIdUser(id)
            .orElseThrow(() -> new RuntimeException("Auxiliar no encontrado"));
        return auxiliary;
    }

    @Transactional
    public Auxiliary createAuxiliary(AuxiliaryDTO dto) {
        if (userService.mailExists(dto.getMail())) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }
        Auxiliary auxiliary = convertToDomain(dto);
        String idCognito = cognitoService.createCognitoUser(convertToUserDTO(dto));
        User user = auxiliary.getUser();
        user.setCognitoId(idCognito);
        auxiliary.setUser(userService.saveUser(user));
        return auxiliaryRepository.save(auxiliary);
    }

    @Transactional
    public Auxiliary updateAuxiliary(Long id, AuxiliaryDTO dto) {
        Auxiliary existingAuxiliary = auxiliaryRepository.findByUserIdUser(id).orElse(null);
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
        cognitoService.updateCognitoUser(convertToUserDTO(dto));
        return auxiliaryRepository.save(existingAuxiliary);
    }

    @Transactional
    public void deleteAuxiliary(Long id) {
        Auxiliary existingAuxiliary = auxiliaryRepository.findByUserIdUser(id).orElse(null);
        if (existingAuxiliary == null) {
            throw new RuntimeException("El Auxiliar con ID " + id + " no existe.");
        }
        cognitoService.deleteCognitoUser(existingAuxiliary.getUser().getMail());
        auxiliaryRepository.deleteByIdUser(id);
        userService.deleteUser(id);
    }
}
