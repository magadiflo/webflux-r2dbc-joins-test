package dev.magadiflo.app.service;

import dev.magadiflo.app.dto.CreateEmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeService {
    Flux<EmployeeResponse> getAllEmployees(String position, Boolean isFullTime);

    Mono<EmployeeResponse> showEmployee(Long employeeId);

    Mono<EmployeeResponse> createEmployee(CreateEmployeeRequest request);

    Mono<EmployeeResponse> updateEmployee(Long employeeId, Employee employee);

    Mono<Void> deleteEmployee(Long employeeId);
}
