package dev.magadiflo.app.integration.repository.impl;

import dev.magadiflo.app.model.entity.Department;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.repository.impl.DepartmentRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DepartmentRepositoryImplTest {

    @Autowired
    private DepartmentRepositoryImpl departmentRepository;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() throws IOException {
        Path dataPath = Paths.get("src/test/resources/data.sql");
        byte[] readData = Files.readAllBytes(dataPath);
        String dataSql = new String(readData);

        this.databaseClient.sql(dataSql)
                .fetch()
                .rowsUpdated()
                .block();
    }

    @Test
    void shouldReturnFluxOfDepartments_whenDataExists() {
        this.departmentRepository.findAll()
                .as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void shouldReturnDepartment_whenValidIdIsProvided() {
        // given
        Long validDepartmentId = 1L;

        // when
        Mono<Department> result = this.departmentRepository.findById(validDepartmentId);

        // then
        StepVerifier.create(result)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getId()).isEqualTo(validDepartmentId);
                    assertThat(departmentDB.getName()).isEqualTo("Tecnología");
                })
                .verifyComplete();
    }

    @Test
    void shouldNotReturnDepartment_whenInvalidIdIsProvided() {
        // given
        Long invalidDepartmentId = 100L;

        // when
        Mono<Department> result = this.departmentRepository.findById(invalidDepartmentId);

        // then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldReturnDepartmentWithManagerAndEmployees_whenValidIdIsProvided() {
        // given
        Long validDepartmentId = 1L;

        // when
        Mono<Department> result = this.departmentRepository.findDepartmentWithManagerAndEmployees(validDepartmentId);

        // then
        StepVerifier.create(result)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getName()).isEqualTo("Tecnología");
                    assertThat(departmentDB.getManager().isPresent()).isTrue();
                    assertThat(departmentDB.getManager().get().getId()).isEqualTo(1L);
                    assertThat(departmentDB.getManager().get().getFirstName()).isEqualTo("Martín");
                    assertThat(departmentDB.getManager().get().getLastName()).isEqualTo("Díaz");
                    assertThat(departmentDB.getManager().get().getPosition()).isEqualTo("Gerente");
                    assertThat(departmentDB.getManager().get().isFullTime()).isTrue();
                    assertThat(departmentDB.getEmployees()).isNotEmpty();
                    assertThat(departmentDB.getEmployees().size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnDepartment_whenValidNameIsProvided() {
        // given
        String validDepartmentName = "Ventas";

        // when
        Mono<Department> result = this.departmentRepository.findByName(validDepartmentName);

        // then
        StepVerifier.create(result)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getId()).isEqualTo(2L);
                    assertThat(departmentDB.getName()).isEqualTo(validDepartmentName);
                    assertThat(departmentDB.getManager()).isNotNull();
                    assertThat(departmentDB.getEmployees().isEmpty()).isFalse();
                })
                .verifyComplete();

    }

    @Test
    void shouldSaveDepartmentWithManagerAndEmployees_whenValidDepartmentIsProvided() {
        // given
        Employee manager = new Employee(null, "Alejandrina", "Flores", "Administradora", true);
        Employee employee1 = new Employee(null, "Evelyn", "Pino", "Vendedor", false);
        Employee employee2 = new Employee(null, "Mónica", "Campos", "Vendedor", true);

        Department department = Department.builder()
                .name("Recursos Humanos")
                .manager(manager)
                .employees(List.of(employee1, employee2))
                .build();

        // when
        Mono<Department> result = this.departmentRepository.save(department);

        // then
        StepVerifier.create(result)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB).isEqualTo(department);
                    assertThat(departmentDB.getManager()).isEqualTo(department.getManager());
                    assertThat(departmentDB.getEmployees().size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateDepartment_whenValidDepartmentIsProvided() {
        // given
        Employee manager = new Employee(1L, "Martín", "Díaz", "Gerente", true);
        Employee employee1 = new Employee(2L, "Katherine", "Fernández", "Desarrollador", true);
        Employee employee2 = new Employee(3L, "Vanessa", "Bello", "Diseñador", false);

        Department department = Department.builder()
                .id(1L)
                .name("TI")
                .manager(manager)
                .employees(List.of(employee1, employee2))
                .build();

        this.departmentRepository.findDepartmentWithManagerAndEmployees(1L)
                .as(StepVerifier::create)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getId()).isEqualTo(1L);
                    assertThat(departmentDB.getName()).isEqualTo("Tecnología");
                    assertThat(departmentDB.getManager()).isNotNull();
                    assertThat(departmentDB.getEmployees().size()).isEqualTo(2);
                })
                .verifyComplete();

        // when
        Mono<Department> result = this.departmentRepository.save(department);

        // then
        StepVerifier.create(result)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB).isEqualTo(department);
                    assertThat(departmentDB.getName()).isEqualTo("TI");
                })
                .verifyComplete();

        this.departmentRepository.findDepartmentWithManagerAndEmployees(1L)
                .as(StepVerifier::create)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getId()).isEqualTo(1L);
                    assertThat(departmentDB.getName()).isEqualTo("TI");
                    assertThat(departmentDB.getManager()).isNotNull();
                    assertThat(departmentDB.getEmployees().size()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void shouldDeleteDepartmentWithManagerAndEmployees_whenValidDepartmentIsProvided() {
        // given
        Employee manager = new Employee(1L, "Martín", "Díaz", "Gerente", true);
        Employee employee1 = new Employee(2L, "Katherine", "Fernández", "Desarrollador", true);
        Employee employee2 = new Employee(3L, "Vanessa", "Bello", "Diseñador", false);

        Department department = Department.builder()
                .id(1L)
                .name("TI")
                .manager(manager)
                .employees(List.of(employee1, employee2))
                .build();

        this.departmentRepository.findDepartmentWithManagerAndEmployees(1L)
                .as(StepVerifier::create)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getId()).isEqualTo(1L);
                    assertThat(departmentDB.getName()).isEqualTo("Tecnología");
                    assertThat(departmentDB.getManager()).isNotNull();
                    assertThat(departmentDB.getEmployees().size()).isEqualTo(2);
                })
                .verifyComplete();

        // when
        Mono<Void> response = this.departmentRepository.delete(department);

        // then
        StepVerifier.create(response)
                .expectNextCount(0)
                .verifyComplete();

        this.departmentRepository.findById(1L)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }
}