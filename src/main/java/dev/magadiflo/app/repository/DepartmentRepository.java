package dev.magadiflo.app.repository;

import dev.magadiflo.app.model.entity.Department;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DepartmentRepository {
    Flux<Department> findAll();

    Mono<Department> findDepartmentWithManagerAndEmployees(Long departmentId);

    Mono<Department> findByName(String name);

    Mono<Department> save(Department department);

    Mono<Void> delete(Department department);
}
