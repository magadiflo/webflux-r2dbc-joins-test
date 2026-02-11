package dev.magadiflo.app.unit.controller;

import dev.magadiflo.app.controller.EmployeeController;
import dev.magadiflo.app.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * 1️⃣ @WebFluxTest(EmployeeController.class)
 * - Crea un slice test (rebanada) solo para la capa web reactiva (WebFlux)
 * - NO levanta tod el contexto de Spring (no repositorios, no servicios reales, no BD)
 * - Solo carga:
 * * - ✅ El controlador especificado (EmployeeController)
 * * - ✅ Componentes necesarios para WebFlux (RouterFunction, filters, exception handlers)
 * * - ✅ WebTestClient (cliente de pruebas reactivas)
 * - ¿Por qué es test UNITARIO?
 * * - Aísla el controlador - El servicio es un mock
 * * - Rápido - No levanta toda la aplicación
 * * - Enfocado - Solo testea lógica del controlador (routing, validaciones, mapeo HTTP)
 * <p>
 * 2️⃣ @Autowired sobre WebTestClient
 * - Cliente HTTP reactivo para tests
 * - Equivalente a MockMvc de Spring MVC, pero para WebFlux
 * - Te permite hacer peticiones HTTP simuladas
 * - @WebFluxTest automáticamente configura y proporciona un WebTestClient, solo necesitas inyectarlo
 * <p>
 * 3️⃣ @MockitoBean
 * - En Spring Boot 3.4+ (tu versión es 3.5.9), @MockBean está deprecated.
 * - Nueva anotación @MockitoBean
 */
@WebFluxTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void shouldReturnAllEmployees() {

    }
}
