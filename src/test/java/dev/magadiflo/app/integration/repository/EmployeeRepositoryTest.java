package dev.magadiflo.app.integration.repository;

import dev.magadiflo.app.config.TestDatabaseConfig;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
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
@ContextConfiguration(classes = {TestDatabaseConfig.class})
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

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
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getFirstName()).isEqualTo("Lizbeth");
                    assertThat(employeeDB.getLastName()).isEqualTo("Gonzales");
                    assertThat(employeeDB.getPosition()).isEqualTo("Teacher");
                    assertThat(employeeDB.isFullTime()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void shouldDoesNotReturnEmployeeWithIdThatDoesNotExist() {
        this.employeeRepository.findById(100L)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeeByPosition() {
        // given
        String position = "Gerente";

        // when
        Flux<Employee> employeesByPosition = this.employeeRepository.findAllByPosition(position);

        // then
        StepVerifier.create(employeesByPosition)
                .expectNextMatches(employee -> employee.getId().equals(1L))
                .expectNextMatches(employee -> employee.getId().equals(4L))
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByFullTime() {
        Flux<Employee> employeesByFullTime = this.employeeRepository.findAllByFullTime(false);

        StepVerifier.create(employeesByFullTime)
                .assertNext(employee -> {
                    assertThat(employee.getId()).isEqualTo(3L);
                    assertThat(employee.getFirstName()).isEqualTo("Vanessa");
                    assertThat(employee.getLastName()).isEqualTo("Bello");
                    assertThat(employee.isFullTime()).isEqualTo(false);
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByPositionAndFullTime() {
        Flux<Employee> employeesByFullTime = this.employeeRepository.findAllByPositionAndFullTime("Teacher", true);

        StepVerifier.create(employeesByFullTime)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldFindAllEmployeesByFirstName() {
        // given

        // when
        Flux<Employee> employeeFlux = this.employeeRepository.findByFirstName("Katherine");

        // then
        StepVerifier.create(employeeFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldSaveAnEmployee() {
        // given
        Employee employee = Employee.builder()
                .firstName("Pepe")
                .lastName("Menis")
                .position("Animador")
                .fullTime(true)
                .build();

        // when
        Mono<Employee> employeeMono = this.employeeRepository.save(employee);

        // then
        StepVerifier.create(employeeMono)
                .assertNext(employeeDB -> {
                    assertThat(employeeDB.getId()).isNotNull();
                    assertThat(employeeDB).isEqualTo(employee);
                })
                .verifyComplete();
    }

    @Test
    void shouldDeleteAnEmployee() {
        // given
        this.employeeRepository.findById(6L)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        // when
        this.employeeRepository.deleteById(6L).block();

        // then
        this.employeeRepository.findById(6L)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }
}