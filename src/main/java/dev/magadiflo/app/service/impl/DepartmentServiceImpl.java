package dev.magadiflo.app.service.impl;

import dev.magadiflo.app.exception.DepartmentAlreadyExistsException;
import dev.magadiflo.app.exception.DepartmentNotFoundException;
import dev.magadiflo.app.model.dto.CreateDepartmentRequest;
import dev.magadiflo.app.model.entity.Department;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.repository.DepartmentRepository;
import dev.magadiflo.app.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Flux<Department> getAllDepartments() {
        return this.departmentRepository.findAll();
    }

    @Override
    public Mono<Department> showDepartmentWithManagerAndEmployees(Long departmentId) {
        return this.departmentRepository.findDepartmentWithManagerAndEmployees(departmentId)
                .switchIfEmpty(Mono.error(new DepartmentNotFoundException(departmentId)));
    }

    @Override
    public Flux<Employee> getEmployeesFromDepartment(Long departmentId, Boolean isFullTime) {
        if (isFullTime != null) {
            return this.departmentRepository.findDepartmentWithManagerAndEmployees(departmentId)
                    .switchIfEmpty(Mono.error(new DepartmentNotFoundException(departmentId)))
                    .flatMapMany(departmentDB -> {
                        Stream<Employee> employeeStream = departmentDB.getEmployees().stream()
                                .filter(employee -> employee.isFullTime() == isFullTime);
                        return Flux.fromStream(employeeStream);
                    });
        }

        return this.departmentRepository.findDepartmentWithManagerAndEmployees(departmentId)
                .switchIfEmpty(Mono.error(new DepartmentNotFoundException(departmentId)))
                .flatMapMany(department -> Flux.fromIterable(department.getEmployees()));
    }

    @Override
    @Transactional
    public Mono<Department> createDepartment(CreateDepartmentRequest departmentRequest) {
        return this.departmentRepository.findByName(departmentRequest.name())
                .flatMap(departmentDB -> Mono.error(new DepartmentAlreadyExistsException(departmentRequest.name())))
                .defaultIfEmpty(Department.builder().name(departmentRequest.name()).build())
                .cast(Department.class)
                .flatMap(this.departmentRepository::save);
    }

    @Override
    @Transactional
    public Mono<Department> updateDepartment(Long departmentId, Department department) {
        return this.departmentRepository.findDepartmentWithManagerAndEmployees(departmentId)
                .switchIfEmpty(Mono.error(new DepartmentNotFoundException(departmentId)))
                .map(departmentDB -> {
                    departmentDB.setName(department.getName());
                    if (department.getManager().isPresent()) {
                        departmentDB.setManager(department.getManager().get());
                    }
                    departmentDB.setEmployees(department.getEmployees());
                    return departmentDB;
                })
                .flatMap(this.departmentRepository::save);
    }

    @Override
    @Transactional
    public Mono<Void> deleteDepartment(Long departmentId) {
        return this.departmentRepository.findDepartmentWithManagerAndEmployees(departmentId)
                .switchIfEmpty(Mono.error(new DepartmentNotFoundException(departmentId)))
                .flatMap(this.departmentRepository::delete);
    }
}
