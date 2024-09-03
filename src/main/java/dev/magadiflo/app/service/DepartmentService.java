package dev.magadiflo.app.service;

import dev.magadiflo.app.model.dto.CreateDepartmentRequest;
import dev.magadiflo.app.model.entity.Department;
import dev.magadiflo.app.model.entity.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DepartmentService {
    Flux<Department> getAllDepartments();

    Mono<Department> showDepartment(Long departmentId);

    Flux<Employee> getEmployeesFromDepartment(Long departmentId, Boolean isFullTime);

    Mono<Department> createDepartment(CreateDepartmentRequest departmentRequest);

    Mono<Department> updateDepartment(Long departmentId, Department department);

    Mono<Void> deleteDepartment(Long departmentId);
}
