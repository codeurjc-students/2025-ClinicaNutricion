package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.Auxiliary;
import com.jorgeleal.clinicanutricion.model.Nutritionist;
import com.jorgeleal.clinicanutricion.repository.AuxiliaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuxiliaryService {

    @Autowired
    private AuxiliaryRepository auxiliaryRepository;

    public List<Auxiliary> getAllAuxiliaries() {
        return auxiliaryRepository.findAll();
    }

    public List<Auxiliary> getAuxiliariesByFilters(String name, String surname, String phone, String email) {
        return auxiliaryRepository.findByUserFilters(name, surname, phone, email);
    }

    public Auxiliary getAuxiliaryById(String id) {
        return auxiliaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auxiliar no encontrado"));
    }

    public Auxiliary createAuxiliary(Auxiliary auxiliary) {
        return auxiliaryRepository.save(auxiliary);
    }

    public Auxiliary updateAuxiliary(String id, Auxiliary updatedAuxiliary) {
        if (!auxiliaryRepository.existsById(id)) {
            throw new RuntimeException("Auxiliar no encontrado");
        }
        updatedAuxiliary.setIdUser(id);
        return auxiliaryRepository.save(updatedAuxiliary);
    }

    public void deleteAuxiliary(String id) {
        if (!auxiliaryRepository.existsById(id)) {
            throw new RuntimeException("Auxiliar no encontrado");
        }
        auxiliaryRepository.deleteById(id);
    }
}
