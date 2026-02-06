package dev.magadiflo.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeeRequest(@NotBlank
                              String firstName,
                              @NotBlank
                              String lastName,
                              @NotBlank
                              String position,
                              @NotNull
                              Boolean fullTime) {
}
