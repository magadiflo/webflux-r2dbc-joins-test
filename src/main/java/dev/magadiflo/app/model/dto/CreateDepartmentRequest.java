package dev.magadiflo.app.model.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDepartmentRequest(@NotBlank
                                      String name) {
}
