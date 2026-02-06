package dev.magadiflo.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeeRequest(Long id,

                              // Campos que sí requieren validación
                              @NotBlank
                              String firstName,
                              @NotBlank
                              String lastName,
                              @NotBlank
                              String position,
                              @NotNull
                              Boolean fullTime) {
}
