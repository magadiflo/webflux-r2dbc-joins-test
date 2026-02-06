package dev.magadiflo.app.service;

import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeService {
    Flux<EmployeeResponse> getAllEmployees(String position, Boolean isFullTime);

    Mono<EmployeeResponse> showEmployee(Long employeeId);

    Mono<EmployeeResponse> createEmployee(EmployeeRequest request);

    Mono<EmployeeResponse> updateEmployee(Long employeeId, EmployeeRequest employeeRequest);

    Mono<Void> deleteEmployee(Long employeeId);
}
