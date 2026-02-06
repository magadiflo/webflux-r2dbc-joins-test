package dev.magadiflo.app.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Objects;

public record DepartmentRequest(@NotBlank
                                String name,

                                // ⚠️ No se usa @Valid intencionalmente.
                                // Los campos de EmployeeRequest NO se validan en este endpoint.
                                // Si en el futuro se requiere validación anidada, agregar @Valid.
                                EmployeeRequest manager,

                                // ⚠️ Tampoco se valida la lista de empleados por diseño.
                                List<EmployeeRequest> employees) {

    public DepartmentRequest {
        employees = Objects.isNull(employees) ? List.of() : employees;
    }
}
