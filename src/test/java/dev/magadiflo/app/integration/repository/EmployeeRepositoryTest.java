package dev.magadiflo.app.integration.repository;

import dev.magadiflo.app.config.TestDatabaseConfig;
import dev.magadiflo.app.entity.Employee;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        // given: IMPLÍCITO - Los datos vienen del @BeforeEach

        // when
        Flux<Employee> result = this.employeeRepository.findAll();

        // then
        StepVerifier.create(result)
                .expectNextCount(7)
                .verifyComplete();
    }

    @Test
    void shouldFindAnEmployee() {
        // given
        Long employeeId = 6L;

        // when
        Mono<Employee> result = this.employeeRepository.findById(employeeId);

        // then
        StepVerifier.create(result)
                .consumeNextWith(employee -> {
                    assertThat(employee)
                            .extracting("id", "firstName", "lastName", "position", "fullTime")
                            .containsExactly(6L, "Lizbeth", "Gonzales", "Teacher", true);
                })
                .verifyComplete();
    }

    @Test
    void shouldDoesNotReturnEmployeeWithIdThatDoesNotExist() {
        // given
        Long employeeId = 8L;

        // when
        Mono<Employee> result = this.employeeRepository.findById(employeeId);

        // then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByPosition() {
        // given
        String position = "Gerente";

        // when
        Flux<Employee> result = this.employeeRepository.findByPosition(position);

        // then
        StepVerifier.create(result)
                .expectNextMatches(employee -> employee.getId().equals(1L))
                .expectNextMatches(employee -> employee.getId().equals(4L))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenFindingByNonExistentPosition() {
        // given
        String position = "CEO";

        // when
        Flux<Employee> result = this.employeeRepository.findByPosition(position);

        // then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeeByFullTime() {
        // given
        boolean isFullTime = false;

        // when
        Flux<Employee> result = this.employeeRepository.findByFullTime(isFullTime);

        // then
        StepVerifier.create(result)
                .assertNext(employee -> {
                    assertThat(employee)
                            .extracting("id", "firstName", "lastName", "position", "fullTime")
                            .containsExactly(3L, "Vanessa", "Bello", "Diseñador", false);
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByPositionAndFullTime() {
        // given
        String position = "Teacher";
        boolean isFullTime = true;

        // when
        Flux<Employee> result = this.employeeRepository.findByPositionAndFullTime(position, isFullTime);

        // then
        StepVerifier.create(result)
                .consumeNextWith(employee -> assertThat(employee.getId()).isEqualTo(6L))
                .consumeNextWith(employee -> assertThat(employee.getId()).isEqualTo(7L))
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByFirstName() {
        // given
        String firstName = "Katherine";

        // when
        Flux<Employee> result = this.employeeRepository.findByFirstName(firstName);

        // then
        StepVerifier.create(result)
                .assertNext(employee -> assertThat(employee.getId()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    void shouldSaveAnEmployee() {
        // given
        Employee employee = Employee.builder()
                .firstName("Lesly")
                .lastName("Águila")
                .position("Vocalista")
                .fullTime(true)
                .build();

        // when
        Mono<Employee> employeeMono = this.employeeRepository.save(employee);

        // then
        StepVerifier.create(employeeMono)
                .assertNext(employeeDB -> {
                    assertThat(employeeDB.getId()).isNotNull();
                    assertThat(employeeDB.getFirstName()).isEqualTo(employee.getFirstName());
                    assertThat(employeeDB.getLastName()).isEqualTo(employee.getLastName());
                    assertThat(employeeDB).isEqualTo(employee);
                }).verifyComplete();
    }

    @Test
    void shouldDeleteAnEmployee() {
        // given
        Long employeeId = 6L;

        // when
        Mono<Void> result = this.employeeRepository.deleteById(employeeId);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        this.employeeRepository.findById(employeeId)
                .as(StepVerifier::create)
                .verifyComplete();
    }
}
