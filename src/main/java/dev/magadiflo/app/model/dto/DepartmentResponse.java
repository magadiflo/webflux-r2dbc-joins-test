package dev.magadiflo.app.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.magadiflo.app.model.entity.Employee;
import lombok.*;

import java.util.List;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DepartmentResponse {
    private Long id;
    private String name;
    private Employee manager;
    private List<Employee> employees;
}
