package dev.magadiflo.app.repository;

import dev.magadiflo.app.entity.Employee;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface EmployeeRepository extends R2dbcRepository<Employee, Long> {
    Flux<Employee> findByPosition(String position);

    Flux<Employee> findByFullTime(Boolean isFullTime);

    Flux<Employee> findByPositionAndFullTime(String position, Boolean isFullTime);

    Flux<Employee> findByFirstName(String firstName);
}
