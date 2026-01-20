package dev.magadiflo.app.mapper;

import dev.magadiflo.app.dto.CreateDepartmentRequest;
import dev.magadiflo.app.dto.DepartmentResponse;
import dev.magadiflo.app.entity.Department;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DepartmentMapper {

    private final EmployeeMapper employeeMapper;

    public DepartmentResponse toBasicDepartmentResponse(final Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                null,
                null);
    }

    public DepartmentResponse toDepartmentResponse(final Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getManager()
                        .map(this.employeeMapper::toEmployeeResponse)
                        .orElseGet(() -> null),
                department.getEmployees().stream()
                        .map(this.employeeMapper::toEmployeeResponse)
                        .toList()
        );
    }

    public Department toDepartment(CreateDepartmentRequest request) {
        return Department.builder()
                .name(request.name())
                .build();
    }
}
