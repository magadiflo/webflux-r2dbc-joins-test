package dev.magadiflo.app.integration.dao;

import dev.magadiflo.app.config.TestDatabaseConfig;
import dev.magadiflo.app.dao.DepartmentDao;
import dev.magadiflo.app.dao.impl.DepartmentDaoImpl;
import dev.magadiflo.app.entity.Department;
import dev.magadiflo.app.entity.Employee;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Import(DepartmentDaoImpl.class)
@ContextConfiguration(classes = TestDatabaseConfig.class)
class DepartmentDaoTest {

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private DatabaseClient databaseClient;

    private static String dataSql;

    @BeforeAll
    static void beforeAll() throws IOException {
        Resource dataResource = new ClassPathResource("sql-test/data.sql");
        Path dataPath = Paths.get(dataResource.getURI());
        byte[] dataRead = Files.readAllBytes(dataPath);
        dataSql = new String(dataRead);
    }

    @BeforeEach
    void setUp() {
        this.databaseClient
                .sql(dataSql)
                .fetch()
                .rowsUpdated()
                .block();
    }

    @Test
    void shouldFindAllDepartments() {
        // when
        Mono<List<Department>> result = this.departmentDao.findAll().collectList();

        // then
        StepVerifier.create(result)
                .assertNext(departments -> {
                    assertThat(departments)
                            .hasSize(4)
                            .extracting(Department::getName)
                            .containsExactly("Tecnología", "Ventas", "Legal", "Soporte");
                })
                .verifyComplete();
    }

    @Test
    void shouldFindADepartmentWithValidId() {
        // given
        Long departmentId = 1L;

        // when
        Mono<Department> departmentMono = this.departmentDao.findById(departmentId);

        // then
        StepVerifier.create(departmentMono)
                .assertNext(department -> {
                    assertThat(department)
                            .extracting(Department::getId, Department::getName, Department::getManager, Department::getEmployees)
                            .containsExactly(departmentId, "Tecnología", Optional.empty(), List.of());
                })
                .verifyComplete();
    }

    @Test
    void shouldNotReturnDepartmentWithInvalidId() {
        // given
        Long departmentId = 100L;

        // when
        Mono<Department> departmentMono = this.departmentDao.findById(departmentId);

        // then
        StepVerifier.create(departmentMono)
                .verifyComplete();
    }

    @Test
    void shouldReturnDepartmentWithManagerAndEmployees() {
        // given
        Long departmentId = 1L;

        // when
        Mono<Department> departmentMono = this.departmentDao.findDepartmentWithManagerAndEmployees(departmentId);

        // then
        StepVerifier.create(departmentMono)
                .assertNext(department -> {
                    assertThat(department.getId()).isEqualTo(departmentId);
                    assertThat(department.getName()).isEqualTo("Tecnología");
                    assertThat(department.getManager())
                            .hasValueSatisfying(manager -> {
                                assertThat(manager.getId()).isEqualTo(1L);
                                assertThat(manager.getFirstName()).isEqualTo("Martín");
                                assertThat(manager.getLastName()).isEqualTo("Díaz");
                                assertThat(manager.getPosition()).isEqualTo("Gerente");
                                assertThat(manager.getFullTime()).isTrue();
                            });
                    assertThat(department.getEmployees())
                            .hasSize(2)
                            .map(Employee::getId)
                            .containsExactlyInAnyOrder(2L, 3L);
                })
                .verifyComplete();
    }

    @Test
    void shouldFindDepartmentByName() {
        // given
        String departmentName = "Legal";

        // when
        Mono<Department> departmentMono = this.departmentDao.findByName(departmentName);

        // then
        StepVerifier.create(departmentMono)
                .assertNext(department -> {
                    assertThat(department.getId()).isEqualTo(3L);
                    assertThat(department.getName()).isEqualTo(departmentName);
                    assertThat(department.getManager()).isEmpty();
                    assertThat(department.getEmployees()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void shouldSaveDepartmentWithManagerAndEmployees() {
        // given
        Employee managerToSave = Employee.builder()
                .firstName("Yrma")
                .lastName("Guerrero")
                .position("Psicóloga")
                .fullTime(true)
                .build();

        List<Employee> employeesToSave = List.of(
                Employee.builder()
                        .firstName("Lesly")
                        .lastName("Águila")
                        .position("Vocalista")
                        .fullTime(true)
                        .build(),
                Employee.builder()
                        .firstName("Susana")
                        .lastName("Alvarado")
                        .position("Vocalista")
                        .fullTime(true)
                        .build()
        );

        Department departmentToSave = Department.builder()
                .name("Música")
                .manager(managerToSave)
                .employees(employeesToSave)
                .build();

        // when
        Mono<Department> departmentMono = this.departmentDao.save(departmentToSave);

        // then
        StepVerifier.create(departmentMono)
                .assertNext(department -> {
                    assertThat(department.getId()).isNotNull();
                    assertThat(department.getName()).isEqualTo("Música");

                    assertThat(department.getManager())
                            .hasValueSatisfying(manager -> {
                                assertThat(manager.getId()).isNotNull();
                                assertThat(manager.getFirstName()).isEqualTo("Yrma");
                                assertThat(manager.getLastName()).isEqualTo("Guerrero");
                                assertThat(manager.getPosition()).isEqualTo("Psicóloga");
                                assertThat(manager.getFullTime()).isTrue();
                            });

                    assertThat(department.getEmployees())
                            .hasSize(2)
                            .allSatisfy(employee -> {
                                assertThat(employee.getId()).isNotNull();
                                assertThat(employee.getPosition()).isEqualTo("Vocalista");
                            });
                    assertThat(department.getEmployees())
                            .extracting(Employee::getId)
                            .doesNotHaveDuplicates();
                    assertThat(department.getEmployees())
                            .map(Employee::getFirstName)
                            .containsExactlyInAnyOrder("Lesly", "Susana");
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateDepartmentWithManagerAndEmployees() {
        // given
        Employee managerToUpdate = Employee.builder()
                .id(2L)
                .firstName("Katherine")
                .lastName("Fernández")
                .position("Desarrollador")
                .fullTime(false)
                .build();

        List<Employee> employeesToUpdate = List.of(
                // Empleado existente en BD
                Employee.builder()
                        .id(7L)
                        .firstName("Jorge")
                        .lastName("Gayoso")
                        .position("Developer")
                        .fullTime(true)
                        .build(),

                // Nuevo empleado a registrar
                Employee.builder()
                        .firstName("Susana")
                        .lastName("Alvarado")
                        .position("Developer")
                        .fullTime(true)
                        .build()
        );

        Department departmentToUpdate = Department.builder()
                .id(2L)
                .name("Sales")
                .manager(managerToUpdate)
                .employees(employeesToUpdate)
                .build();

        // when
        Mono<Department> departmentMono = this.departmentDao.save(departmentToUpdate);

        // then
        StepVerifier.create(departmentMono)
                .assertNext(department -> {
                    assertThat(department.getId()).isEqualTo(2L);
                    assertThat(department.getName()).isEqualTo("Sales");

                    assertThat(department.getManager())
                            .hasValueSatisfying(manager -> {
                                assertThat(manager.getId()).isNotNull();
                                assertThat(manager.getFirstName()).isEqualTo("Katherine");
                                assertThat(manager.getLastName()).isEqualTo("Fernández");
                                assertThat(manager.getPosition()).isEqualTo("Desarrollador");
                                assertThat(manager.getFullTime()).isFalse();
                            });

                    assertThat(department.getEmployees())
                            .hasSize(2)
                            .allSatisfy(employee -> {
                                assertThat(employee.getId()).isNotNull();
                                assertThat(employee.getPosition()).isEqualTo("Developer");
                            });
                    assertThat(department.getEmployees())
                            .extracting(Employee::getId)
                            .doesNotHaveDuplicates();
                    assertThat(department.getEmployees())
                            .map(Employee::getFirstName)
                            .containsExactlyInAnyOrder("Jorge", "Susana");
                })
                .verifyComplete();
    }

    @Test
    void shouldDeleteDepartmentWithManagerAndEmployees() {
        // given
        Long departmentId = 1L;
        Department departmentToDelete = Department.builder().id(departmentId).build();

        // when
        Mono<Void> deleteMono = this.departmentDao.delete(departmentToDelete);

        // then
        StepVerifier.create(deleteMono)
                .verifyComplete();

        StepVerifier.create(this.departmentDao.findDepartmentWithManagerAndEmployees(departmentId))
                .verifyComplete();
    }
}
