package com.jorgeleal.clinicanutricion.dto; 

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jorgeleal.clinicanutricion.model.Gender; 
import jakarta.validation.constraints.*; 
import lombok.Getter; 
import lombok.Setter; 
import java.time.LocalDate; 
import java.time.LocalTime; 

@Getter 
@Setter 
public class NutritionistDTO {
    @NotNull 
    private Long idUser;

    @NotNull
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El nombre solo puede contener letras y espacios.")
    private String name;
    
    @NotNull
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres.")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "El apellido solo puede contener letras y espacios.")
    private String surname;
    
    @NotNull
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    @Min(value = 1900, message = "El año de nacimiento no puede ser menor a 1900.")
    private LocalDate birthDate;
    
    @NotNull
    @Email(message = "El email no es válido.")
    private String mail;
    
    @NotNull
    @Pattern(regexp = "^\\+\\d{1,3}\\d{6,14}$", message = "El teléfono debe incluir código de país seguido del número, sin espacios ni guiones.")
    private String phone;

    @NotNull
    private boolean active;
    
    @NotNull
    private Gender gender;

    @NotNull
    private int appointmentDuration;

    @NotNull
    private int maxActiveAppointments;

    @NotNull(message = "Debes introducir la hora de inicio.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Debes introducir la hora de fin.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @SuppressWarnings("PMD.UnusedPrivateMethod") // PMD no detecta métodos invocados por anotaciones
    @AssertTrue(message = "La hora de inicio y fin deben estar entre 09:00 - 20:00 y la hora de fin debe ser mayor a la de inicio.")
    private boolean isTimeWindowValid() {
        if (startTime == null || endTime == null) {
            return true; 
        }
        LocalTime min = LocalTime.of(9, 0);
        LocalTime max = LocalTime.of(20, 0);
        return !startTime.isBefore(min)
            && !endTime.isBefore(min)
            && !startTime.isAfter(max)
            && !endTime.isAfter(max)
            && endTime.isAfter(startTime);
    }
}
