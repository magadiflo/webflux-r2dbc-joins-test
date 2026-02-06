package dev.magadiflo.app.service;

import dev.magadiflo.app.dto.DepartmentRequest;
import dev.magadiflo.app.dto.DepartmentResponse;
import dev.magadiflo.app.dto.EmployeeResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DepartmentService {
    Flux<DepartmentResponse> getAllDepartments();

    Mono<DepartmentResponse> showDepartment(Long departmentId);

    Mono<DepartmentResponse> showDepartmentWithManagerAndEmployees(Long departmentId);

    Mono<DepartmentResponse> createDepartment(DepartmentRequest request);

    Mono<DepartmentResponse> updateDepartment(Long departmentId, DepartmentRequest departmentRequest);

    Mono<Void> deleteDepartment(Long departmentId);

    Flux<EmployeeResponse> getEmployeesFromDepartment(Long departmentId, Boolean isFullTime);
}
