package dev.magadiflo.app.service;

import dev.magadiflo.app.model.dto.CreateEmployeeRequest;
import dev.magadiflo.app.model.entity.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeService {
    Flux<Employee> getAllEmployees(String position, Boolean isFullTime);

    Mono<Employee> showEmployee(Long employeeId);

    Mono<Employee> createEmployee(CreateEmployeeRequest employeeRequest);

    Mono<Employee> updateEmployee(Long employeeId, Employee employee);

    Mono<Void> deleteEmployee(Long employeeId);
}
