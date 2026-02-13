package dev.magadiflo.app.integration.controller;

import dev.magadiflo.app.config.TestDatabaseConfig;
import dev.magadiflo.app.dto.EmployeeResponse;
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
                    assertThat(employeeResponses)
                            .isNotNull();
                    assertThat(employeeResponses.getFirst())
                            .extracting(EmployeeResponse::id, EmployeeResponse::firstName)
                            .containsExactly(1L, "Martín");
                });
    }
}
