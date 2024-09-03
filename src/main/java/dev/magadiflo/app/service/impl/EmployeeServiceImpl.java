package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.model.dto.CreateEmployeeRequest;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.repository.EmployeeRepository;
import dev.magadiflo.app.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;


    @Override
    public Flux<Employee> getAllEmployees(String position, Boolean isFullTime) {
        if (position == null && isFullTime == null) {
            return this.employeeRepository.findAll();
        }

        if (position != null & isFullTime != null) {
            return this.employeeRepository.findAllByPositionAndFullTime(position, isFullTime);
        }

        if (position != null) {
            return this.employeeRepository.findAllByPosition(position);
        }

        return this.employeeRepository.findAllByFullTime(isFullTime);
    }

    @Override
    public Mono<Employee> showEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)));
    }

    @Override
    @Transactional
    public Mono<Employee> createEmployee(CreateEmployeeRequest employeeRequest) {
        Employee employee = Employee.builder()
                .firstName(employeeRequest.firstName())
                .lastName(employeeRequest.lastName())
                .position(employeeRequest.position())
                .fullTime(employeeRequest.isFullTime())
                .build();
        return this.employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Mono<Employee> updateEmployee(Long employeeId, Employee employee) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                .map(employeeDB -> {
                    employeeDB.setFirstName(employee.getFirstName());
                    employeeDB.setLastName(employee.getLastName());
                    employeeDB.setPosition(employee.getPosition());
                    employeeDB.setFullTime(employee.isFullTime());
                    return employeeDB;
                })
                .flatMap(this.employeeRepository::save);
    }

    @Override
    @Transactional
    public Mono<Void> deleteEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                .flatMap(this.employeeRepository::delete)
                .then();
    }
}
