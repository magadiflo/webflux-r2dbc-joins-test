package dev.magadiflo.app.repository.impl;

import dev.magadiflo.app.model.entity.Department;
import dev.magadiflo.app.repository.DepartmentRepository;
import dev.magadiflo.app.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Repository
public class DepartmentRepositoryImpl implements DepartmentRepository {

    private final EmployeeRepository employeeRepository;
    private final DatabaseClient client;

    @Override
    public Flux<Department> findAll() {
        return null;
    }

    @Override
    public Mono<Department> findById(Long departmentId) {
        return null;
    }

    @Override
    public Mono<Department> findByName(String name) {
        return null;
    }

    @Override
    public Mono<Department> save(Department department) {
        return null;
    }

    @Override
    public Mono<Void> delete(Department department) {
        return null;
    }
}
