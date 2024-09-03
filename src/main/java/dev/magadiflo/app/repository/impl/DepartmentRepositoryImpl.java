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
    private static final String SELECT_QUERY = """
            SELECT d.id d_id, d.name d_name, m.id m_id, m.first_name m_firstName, m.last_name m_lastName,
                    m.position m_position, m.is_full_time m_isFullTime, e.id e_id, e.first_name e_firstName,
                    e.last_name e_lastName, e.position e_position, e.is_full_time e_isFullTime
            FROM departments d
                LEFT JOIN department_managers dm ON dm.department_id = d.id
                LEFT JOIN employees m ON m.id = dm.employee_id
                LEFT JOIN department_employees de ON de.department_id = d.id
                LEFT JOIN employees e ON e.id = de.employee_id
            """;

    @Override
    public Flux<Department> findAll() {
        return this.client.sql(SELECT_QUERY)
                .fetch()
                .all()
                .bufferUntilChanged(result -> result.get("d_id"))
                .flatMap(Department::fromRows);
    }

    @Override
    public Mono<Department> findById(Long departmentId) {
        return this.client.sql("%s WHERE d.id = :departmentId".formatted(SELECT_QUERY))
                .bind("departmentId", departmentId)
                .fetch()
                .all()
                .bufferUntilChanged(result -> result.get("d_id"))
                .flatMap(Department::fromRows)
                .singleOrEmpty();
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
