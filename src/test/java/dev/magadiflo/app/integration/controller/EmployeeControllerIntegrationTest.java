package dev.magadiflo.app.integration.controller;

import dev.magadiflo.app.config.TestDatabaseConfig;
import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;

import dev.magadiflo.app.fixtures.EmployeeFixture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Tests de integración para EmployeeController.
 * <p>
 * 1️⃣ @SpringBootTest(webEnvironment = RANDOM_PORT)
 * - Levanta T0D0 el contexto de Spring (todos los beans, servicios, repositorios, configuraciones)
 * - Inicia un servidor HTTP real en un puerto aleatorio
 * - NO hay mocks - T0d0 es real
 * - Registra un bean TestRestTemplate y/o WebTestClient para su uso en pruebas web que utilizan un
 * servidor web en pleno funcionamiento.
 * <p>
 * 2️⃣ @ContextConfiguration(classes = TestDatabaseConfig.class)
 * - Carga tu configuración de BD de prueba
 * - Ejecuta el ConnectionFactoryInitializer que puebla schema.sql y data.sql
 */

/**
 * * @ActiveProfiles("test")
 * <p>
 * - Activa uno o más perfiles de Spring para esa clase de test específica.
 * - Es equivalente a activar el perfil programáticamente para ese test (spring.profiles.active: test).
 * - Se usa exclusivamente en clases de test (src/test).
 * - Archivos cargados:
 * * - `application.yml` (base) ✅
 * * - `application-test.yml` (perfil "test") ✅
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestDatabaseConfig.class)
class EmployeeControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

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

    // ===== GET /api/v1/employees/stream - findAllEmployees =====
    @Test
    void shouldReturnAllEmployees_whenNoFiltersProvided() {
        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(EmployeeResponse.class)
                .hasSize(7) // 7 empleados en src/test/resources/sql-test/data.sql
                .consumeWith(result -> {
                    List<EmployeeResponse> employeeResponses = result.getResponseBody();
                    assertThat(employeeResponses).isNotEmpty();
                    assertThat(employeeResponses.getFirst())
                            .extracting(EmployeeResponse::id, EmployeeResponse::firstName)
                            .containsExactly(1L, "Martín");
                });
    }

    @Test
    void shouldReturnFilteredEmployees_whenPositionProvided() {
        // given
        String position = "Gerente";

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/employees/stream")
                        .queryParam("position", position)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBodyList(EmployeeResponse.class)
                .hasSize(2)
                .consumeWith(result -> {
                    List<EmployeeResponse> employeeResponses = result.getResponseBody();
                    assertThat(employeeResponses)
                            .extracting(EmployeeResponse::position)
                            .containsOnly(position);
                });
    }

    @Test
    void shouldReturnFilteredEmployees_whenFullTimeProvided() {
        // given
        Boolean fullTime = false; // Según data.sql: solo Vanessa (id=3)

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/employees/stream")
                        .queryParam("fullTime", fullTime)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBodyList(EmployeeResponse.class)
                .hasSize(1)
                .consumeWith(result -> {
                    List<EmployeeResponse> employeeResponses = result.getResponseBody();
                    assertThat(employeeResponses).isNotEmpty();
                    assertThat(employeeResponses.getFirst())
                            .extracting(EmployeeResponse::id, EmployeeResponse::firstName, EmployeeResponse::lastName, EmployeeResponse::position, EmployeeResponse::fullTime)
                            .containsExactly(3L, "Vanessa", "Bello", "Diseñador", false);
                });
    }

    @Test
    void shouldReturnFilteredEmployees_whenBothFiltersProvided() {
        // given
        String position = "Teacher";  // Según data.sql: Lizbeth (id=6) y Jorge (id=7)
        Boolean fullTime = true;

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/employees/stream")
                        .queryParam("position", position)
                        .queryParam("fullTime", fullTime)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBodyList(EmployeeResponse.class)
                .hasSize(2)
                .consumeWith(result -> {
                    List<EmployeeResponse> employeeResponses = result.getResponseBody();
                    assertThat(employeeResponses)
                            .extracting(EmployeeResponse::position, EmployeeResponse::fullTime)
                            .containsOnly(tuple(position, fullTime));
                });
    }

    // ===== GET /api/v1/employees/{employeeId} - findEmployee =====
    @Test
    void shouldReturnEmployee_whenValidIdProvided() {
        // given
        Long employeeId = 1L; // Martín Díaz según data.sql

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/{employeeId}", employeeId)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EmployeeResponse.class)
                .consumeWith(result -> {
                    EmployeeResponse employeeResponse = result.getResponseBody();
                    assertThat(employeeResponse)
                            .extracting(EmployeeResponse::id, EmployeeResponse::firstName, EmployeeResponse::lastName, EmployeeResponse::position, EmployeeResponse::fullTime)
                            .containsExactly(1L, "Martín", "Díaz", "Gerente", true);
                });
    }

    @Test
    void shouldReturn404_whenEmployeeNotFound() {
        // given
        Long nonExistentId = 999L;

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/{employeeId}", nonExistentId)
                .exchange();

        // then
        response.expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Empleado no encontrado")
                .jsonPath("$.detail").isEqualTo("El empleado con id [999] no fue encontrado");
    }

    // ===== POST /api/v1/employees - saveEmployee =====
    @Test
    void shouldCreateEmployee_whenValidRequestProvided() {
        // given
        EmployeeRequest request = EmployeeFixture.createDefaultRequest();

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.post()
                .uri("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)      // Tipo de contenido del request (lo que envío)
                .accept(MediaType.APPLICATION_JSON)  // Tipo de contenido esperado en la respuesta (Accept header)
                .bodyValue(request)
                .exchange();

        // then
        response.expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EmployeeResponse.class)
                .consumeWith(result -> {
                    EmployeeResponse employeeResponse = result.getResponseBody();
                    assertThat(employeeResponse)
                            .extracting(EmployeeResponse::id, EmployeeResponse::firstName, EmployeeResponse::lastName, EmployeeResponse::position, EmployeeResponse::fullTime)
                            .containsExactly(8L, request.firstName(), request.lastName(), request.position(), request.fullTime());
                });

        // Verifica que realmente se guardó en la BD
        this.webTestClient.get()
                .uri("/api/v1/employees/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EmployeeResponse.class)
                .hasSize(8);  // 7 originales + 1 nuevo
    }

    @Test
    void shouldReturn400_whenCreatingEmployeeWithInvalidData() {
        // given
        EmployeeRequest invalidRequest = new EmployeeRequest(
                null,
                "",  // firstName vacío (violación @NotBlank)
                "",  // lastName vacío
                "  ",  // position vacío
                null // fullTime null (violación @NotNull)
        );

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.post()
                .uri("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange();

        // then
        response.expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Error de validación de campos")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errors.firstName").exists()
                .jsonPath("$.errors.firstName[0]").isEqualTo("must not be blank")
                .jsonPath("$.errors.lastName").exists()
                .jsonPath("$.errors.lastName[0]").isEqualTo("must not be blank")
                .jsonPath("$.errors.position").exists()
                .jsonPath("$.errors.position[0]").isEqualTo("must not be blank")
                .jsonPath("$.errors.fullTime").exists()
                .jsonPath("$.errors.fullTime[0]").isEqualTo("must not be null");
    }

    // ===== PUT /api/v1/employees/{employeeId} - updateEmployee =====
    @Test
    void shouldUpdateEmployee_whenValidDataProvided() {
        // given
        Long employeeId = 1L;
        EmployeeRequest request = EmployeeFixture.createDefaultRequest();

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.put()
                .uri("/api/v1/employees/{employeeId}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)      // Tipo de contenido del request (lo que envío)
                .accept(MediaType.APPLICATION_JSON)  // Tipo de contenido esperado en la respuesta (Accept header)
                .bodyValue(request)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(employeeId)
                .jsonPath("$.firstName").isEqualTo(request.firstName())
                .jsonPath("$.lastName").isEqualTo(request.lastName())
                .jsonPath("$.position").isEqualTo(request.position())
                .jsonPath("$.fullTime").isEqualTo(request.fullTime());

        // Verifica que el cambio persiste en la BD
        this.webTestClient.get()
                .uri("/api/v1/employees/{employeeId}", employeeId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeResponse.class)
                .consumeWith(result -> {
                    EmployeeResponse employeeResponse = result.getResponseBody();
                    assertThat(employeeResponse)
                            .extracting(EmployeeResponse::firstName)
                            .isEqualTo(request.firstName());
                });
    }

    @Test
    void shouldReturn404_whenUpdatingNonExistentEmployee() {
        // given
        Long nonExistentId = 999L;
        EmployeeRequest request = EmployeeFixture.createUpdateRequest();

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.put()
                .uri("/api/v1/employees/{employeeId}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange();

        // then
        response.expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Empleado no encontrado")
                .jsonPath("$.detail").isEqualTo("El empleado con id [999] no fue encontrado");
    }

    @Test
    void shouldReturn400_whenUpdatingWithInvalidData() {
        // given
        Long employeeId = 1L;
        EmployeeRequest invalidRequest = new EmployeeRequest(
                null,
                "",  // firstName vacío (violación @NotBlank)
                "",  // lastName vacío
                "  ",  // position vacío
                null // fullTime null (violación @NotNull)
        );

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.put()
                .uri("/api/v1/employees/{employeeId}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange();

        // then
        response.expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Error de validación de campos")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errors.firstName").exists()
                .jsonPath("$.errors.firstName[0]").isEqualTo("must not be blank")
                .jsonPath("$.errors.lastName").exists()
                .jsonPath("$.errors.lastName[0]").isEqualTo("must not be blank")
                .jsonPath("$.errors.position").exists()
                .jsonPath("$.errors.position[0]").isEqualTo("must not be blank")
                .jsonPath("$.errors.fullTime").exists()
                .jsonPath("$.errors.fullTime[0]").isEqualTo("must not be null");
    }

//    // ===== DELETE /api/v1/employees/{employeeId} - deleteEmployee =====
//    @Test
//    void shouldDeleteEmployee_whenValidIdProvided() {
//        // given
//        Long employeeId = 1L;
//        when(this.employeeService.deleteEmployee(employeeId))
//                .thenReturn(Mono.empty());
//
//        // when
//        WebTestClient.ResponseSpec response = this.webTestClient.delete()
//                .uri("/api/v1/employees/{employeeId}", employeeId)
//                .exchange();
//
//        // then
//        response.expectStatus().isNoContent()
//                .expectBody().isEmpty();
//
//        verify(this.employeeService).deleteEmployee(employeeId);
//    }
//
//    @Test
//    void shouldReturn404_whenDeletingNonExistentEmployee() {
//        // given
//        Long nonExistentId = 999L;
//
//        when(this.employeeService.deleteEmployee(nonExistentId))
//                .thenReturn(Mono.error(() -> new EmployeeNotFoundException(nonExistentId)));
//
//        // when
//        WebTestClient.ResponseSpec response = this.webTestClient.delete()
//                .uri("/api/v1/employees/{employeeId}", nonExistentId)
//                .exchange();
//
//        // then
//        response.expectStatus().isNotFound()
//                .expectBody()
//                .jsonPath("$.title").isEqualTo("Empleado no encontrado")
//                .jsonPath("$.detail").isEqualTo("El empleado con id [999] no fue encontrado");
//
//        verify(this.employeeService).deleteEmployee(nonExistentId);
//    }
}
