package dev.magadiflo.app.integration.controller;

import dev.magadiflo.app.config.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

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

    //TODO: implementar test
}
