package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.dto.CreateEmployeeRequest;
import dev.magadiflo.app.entity.Employee;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.repository.EmployeeRepository;
import dev.magadiflo.app.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    public Flux<Employee> getAllEmployees(String position, Boolean isFullTime) {
        if (Objects.isNull(position) && Objects.isNull(isFullTime)) {
            return this.employeeRepository.findAll();
        }

        if (Objects.nonNull(position) && Objects.nonNull(isFullTime)) {
            return this.employeeRepository.findByPositionAndFullTime(position, isFullTime);
        }

        if (Objects.nonNull(position)) {
            return this.employeeRepository.findByPosition(position);
        }

        return this.employeeRepository.findByFullTime(isFullTime);
    }

    @Override
    public Mono<Employee> showEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)));
    }

    @Override
    @Transactional
    public Mono<Employee> createEmployee(CreateEmployeeRequest request) {
        return this.employeeRepository.save(Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .position(request.position())
                .fullTime(request.isFullTime())
                .build()
        );
    }

    @Override
    @Transactional
    public Mono<Employee> updateEmployee(Long employeeId, Employee employee) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                .map(employeeDB -> {
                    log.info("Empleado encontrado: {}", employeeDB);
                    employeeDB.setFirstName(employee.getFirstName());
                    employeeDB.setLastName(employee.getLastName());
                    employeeDB.setPosition(employee.getPosition());
                    employeeDB.setFullTime(employee.getFullTime());
                    return employeeDB;
                })
                .flatMap(this.employeeRepository::save)
                .doOnNext(employeeDB -> log.info("Empleado actualizado: {}", employeeDB));
    }

    @Override
    @Transactional
    public Mono<Void> deleteEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(new EmployeeNotFoundException(employeeId)))
                .flatMap(this.employeeRepository::delete);
    }
}
