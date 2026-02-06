package dev.magadiflo.app.integration.dao;

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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(DepartmentDaoImpl.class)
@DataR2dbcTest
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
        Mono<List<Department>> collectList = this.departmentDao.findAll().collectList();

        StepVerifier.create(collectList)
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
                    assertThat(department.getId()).isEqualTo(departmentId);
                    assertThat(department.getName()).isEqualTo("Tecnología");
                    assertThat(department.getManager()).isEmpty();
                    assertThat(department.getEmployees()).isEmpty();
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
}
