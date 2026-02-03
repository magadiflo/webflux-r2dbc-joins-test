package dev.magadiflo.app.integration.repository;

import dev.magadiflo.app.config.TestDatabaseConfig;
import dev.magadiflo.app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ContextConfiguration;

import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ContextConfiguration(classes = TestDatabaseConfig.class)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

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
    void shouldFindAllEmployees() {
        this.employeeRepository.findAll()
                .as(StepVerifier::create)
                .expectNextCount(7)
                .verifyComplete();
    }

    @Test
    void shouldFindAnEmployee() {
        this.employeeRepository.findById(6L)
                .as(StepVerifier::create)
                .consumeNextWith(employee -> {
                    assertThat(employee)
                            .extracting("id", "firstName", "lastName", "position", "fullTime")
                            .containsExactly(6L, "Lizbeth", "Gonzales", "Teacher", true);
                })
                .verifyComplete();
    }

    @Test
    void shouldDoesNotReturnEmployeeWithIdThatDoesNotExist() {
        this.employeeRepository.findById(8L)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByPosition() {
        this.employeeRepository.findByPosition("Gerente")
                .as(StepVerifier::create)
                .expectNextMatches(employee -> employee.getId().equals(1L))
                .expectNextMatches(employee -> employee.getId().equals(4L))
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeeByFullTime() {
        this.employeeRepository.findByFullTime(false)
                .as(StepVerifier::create)
                .assertNext(employee -> {
                    assertThat(employee.getId()).isEqualTo(3L);
                    assertThat(employee.getFirstName()).isEqualTo("Vanessa");
                    assertThat(employee.getLastName()).isEqualTo("Bello");
                    assertThat(employee.getPosition()).isEqualTo("Diseñador");
                    assertThat(employee.getFullTime()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByPositionAndFullTime() {
        this.employeeRepository.findByPositionAndFullTime("Teacher", true)
                .as(StepVerifier::create)
                .consumeNextWith(employee -> assertThat(employee.getId()).isEqualTo(6L))
                .consumeNextWith(employee -> assertThat(employee.getId()).isEqualTo(7L))
                .verifyComplete();
    }
}
