package com.jorgeleal.clinicanutricion.service;

import com.jorgeleal.clinicanutricion.model.Availability;
import com.jorgeleal.clinicanutricion.repository.AvailabilityRepository;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    public Availability saveAvailability(Availability availability) {
        return availabilityRepository.save(availability);
    }
}
