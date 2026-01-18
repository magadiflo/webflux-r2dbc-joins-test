package dev.magadiflo.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DepartmentResponse(Long id,
                                 String name,
                                 EmployeeResponse manager,
                                 List<EmployeeResponse> employees) {
}
