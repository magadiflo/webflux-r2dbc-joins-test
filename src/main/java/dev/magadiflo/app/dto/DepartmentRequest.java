package dev.magadiflo.app.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(@NotBlank
                                String name) {
}
