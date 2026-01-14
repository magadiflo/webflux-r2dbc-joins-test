package dev.magadiflo.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.magadiflo.app.entity.Employee;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DepartmentResponse(Long id,
                                 String name,
                                 Employee manager,
                                 List<EmployeeResponse> employees) {
}
