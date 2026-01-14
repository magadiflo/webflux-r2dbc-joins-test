package dev.magadiflo.app.dto;

public record EmployeeResponse(Long id,
                               String firstName,
                               String lastName,
                               String position,
                               Boolean fullTime) {
}
