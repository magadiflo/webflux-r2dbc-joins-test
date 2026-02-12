package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Employee;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.mapper.EmployeeMapper;
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
    private final EmployeeMapper employeeMapper;

    @Override
    public Flux<EmployeeResponse> getAllEmployees(String position, Boolean isFullTime) {
        if (Objects.isNull(position) && Objects.isNull(isFullTime)) {
            return this.employeeRepository.findAll()
                    .map(this.employeeMapper::toEmployeeResponse);
        }

        if (Objects.nonNull(position) && Objects.nonNull(isFullTime)) {
            return this.employeeRepository.findByPositionAndFullTime(position, isFullTime)
                    .map(this.employeeMapper::toEmployeeResponse);
        }

        if (Objects.nonNull(position)) {
            return this.employeeRepository.findByPosition(position)
                    .map(this.employeeMapper::toEmployeeResponse);
        }

        return this.employeeRepository.findByFullTime(isFullTime)
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    public Mono<EmployeeResponse> showEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(() -> new EmployeeNotFoundException(employeeId)))
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    @Transactional
    public Mono<EmployeeResponse> createEmployee(EmployeeRequest request) {
        return this.employeeRepository.save(Employee.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .position(request.position())
                        .fullTime(request.fullTime())
                        .build()
                )
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    @Transactional
    public Mono<EmployeeResponse> updateEmployee(Long employeeId, EmployeeRequest employeeRequest) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(() -> new EmployeeNotFoundException(employeeId)))
                .map(employeeDB -> {
                    log.info("Empleado encontrado: {}", employeeDB);
                    employeeDB.setFirstName(employeeRequest.firstName());
                    employeeDB.setLastName(employeeRequest.lastName());
                    employeeDB.setPosition(employeeRequest.position());
                    employeeDB.setFullTime(employeeRequest.fullTime());
                    return employeeDB;
                })
                .flatMap(this.employeeRepository::save)
                .doOnNext(employeeDB -> log.info("Empleado actualizado: {}", employeeDB))
                .map(this.employeeMapper::toEmployeeResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteEmployee(Long employeeId) {
        return this.employeeRepository.findById(employeeId)
                .switchIfEmpty(Mono.error(() -> new EmployeeNotFoundException(employeeId)))
                .flatMap(this.employeeRepository::delete);
    }
}
