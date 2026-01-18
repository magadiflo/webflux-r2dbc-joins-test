package dev.magadiflo.app.service;

import dev.magadiflo.app.dto.CreateDepartmentRequest;
import dev.magadiflo.app.dto.DepartmentResponse;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Department;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DepartmentService {
    Flux<DepartmentResponse> getAllDepartments();

    Mono<DepartmentResponse> showDepartment(Long departmentId);

    Mono<DepartmentResponse> showDepartmentWithManagerAndEmployees(Long departmentId);

    Mono<DepartmentResponse> createDepartment(CreateDepartmentRequest request);

    Mono<DepartmentResponse> updateDepartment(Long departmentId, Department department);

    Mono<Void> deleteDepartment(Long departmentId);

    Mono<EmployeeResponse> getEmployeesFromDepartment(Long departmentId, Boolean isFullTime);
}
