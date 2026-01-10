package dev.magadiflo.app.dao.impl;

import dev.magadiflo.app.dao.DepartmentDao;
import dev.magadiflo.app.entity.Department;
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
public class DepartmentDaoImpl implements DepartmentDao {

    private final DatabaseClient client;
    private final EmployeeRepository employeeRepository;
    private static final String SELECT_QUERY = """
            SELECT d.id AS d_id,
                    d.name AS d_name,
                    m.id AS m_id,
                    m.first_name AS m_firstName,
                    m.last_name AS m_lastName,
                    m.position AS m_position,
                    m.is_full_time AS m_isFullTime,
                    e.id AS e_id,
                    e.first_name AS e_firstName,
                    e.last_name AS e_lastName,
                    e.position AS e_position,
                    e.is_full_time AS e_isFullTime
            FROM departments AS d
                LEFT JOIN department_managers AS dm ON(d.id = dm.department_id)
                LEFT JOIN employees AS m ON(dm.employee_id = m.id)
                LEFT JOIN department_employees AS de ON(d.id = de.department_id)
                LEFT JOIN employees AS e ON(de.employee_id = e.id)
            """;

    @Override
    public Flux<Department> findAll() {
        return this.client.sql("%s ORDER BY d.id".formatted(SELECT_QUERY))
                .fetch()
                .all()
                .bufferUntilChanged(rowMap -> rowMap.get("d_id"))
                .flatMap(Department::fromRows);
    }

    @Override
    public Mono<Department> findById(Long departmentId) {
        return this.client.sql("""
                        SELECT id, name
                        FROM departments
                        WHERE id = :departmentId
                        """)
                .bind("departmentId", departmentId)
                .map((row, rowMetadata) -> Department.builder()
                        .id(row.get("id", Long.class))
                        .name(row.get("name", String.class))
                        .build()
                )
                .first();
    }

    @Override
    public Mono<Department> findDepartmentWithManagerAndEmployees(Long departmentId) {
        return this.client.sql("%s WHERE d.id = :departmentId".formatted(SELECT_QUERY))
                .bind("departmentId", departmentId)
                .fetch()
                .all()
                .collectList()
                .flatMap(Department::fromRows);
    }

    @Override
    public Mono<Department> findByName(String name) {
        return this.client.sql("%s WHERE d.name = :departmentName".formatted(SELECT_QUERY))
                .bind("departmentName", name)
                .fetch()
                .all()
                .collectList()
                .flatMap(Department::fromRows);
    }

    @Override
    public Mono<Department> save(Department department) {
        return null;
    }

    @Override
    public Mono<Void> delete(Department department) {
        return this.deleteDepartmentManager(department)
                .flatMap(this::deleteDepartmentEmployee)
                .flatMap(this::deleteDepartment);
    }

    private Mono<Department> deleteDepartmentManager(Department department) {
        return this.client.sql("DELETE FROM department_managers WHERE department_id = :departmentId")
                .bind("departmentId", department.getId())
                .fetch()
                .rowsUpdated()
                .thenReturn(department);
    }

    private Mono<Department> deleteDepartmentEmployee(Department department) {
        return this.client.sql("DELETE FROM department_employees WHERE department_id = :departmentId")
                .bind("departmentId", department.getId())
                .fetch()
                .rowsUpdated()
                .thenReturn(department);
    }

    private Mono<Void> deleteDepartment(Department department) {
        return this.client.sql("DELETE FROM departments WHERE id = :departmentId")
                .bind("departmentId", department.getId())
                .fetch()
                .rowsUpdated()
                .then();
    }
}
