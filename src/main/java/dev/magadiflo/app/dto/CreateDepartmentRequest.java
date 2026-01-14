package dev.magadiflo.app.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDepartmentRequest(@NotBlank
                                      String name) {
}
