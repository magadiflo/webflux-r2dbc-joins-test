package dev.magadiflo.app.unit.controller;

import dev.magadiflo.app.controller.EmployeeController;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.fixtures.EmployeeFixture;
import dev.magadiflo.app.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // ===== GET /api/v1/employees/stream - findAllEmployees =====
    @Test
    void shouldReturnAllEmployees_whenNoFiltersProvided() {
        // given
        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDeveloper(1L, true));
        EmployeeResponse response2 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDesigner(2L, false));
        EmployeeResponse response3 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDeveloper(3L, false));

        when(this.employeeService.getAllEmployees(null, null))
                .thenReturn(Flux.just(response1, response2, response3));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()      // Inicia una petición HTTP GET
                .uri("/api/v1/employees/stream")                        // Define la URI del endpoint
                .accept(MediaType.TEXT_EVENT_STREAM)    // Configuración de petición (Request Headers)
                .exchange();                                                // Ejecuta la petición HTTP y obtiene la respuesta

        //---- A partir de aquí, todas las líneas son VERIFICACIONES de la respuesta ----

        // then
        response.expectStatus().isOk()                                                            // Verifica que el status HTTP sea 200 OK
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)  // Verifica que el servidor respondió (Response) con Content-Type: text/event-stream. "Compatible" significa que acepta variaciones como "text/event-stream;charset=UTF-8"
                .expectBodyList(EmployeeResponse.class)                                // Espera una lista de objetos EmployeeResponse en el body
                .hasSize(3)                                                                  // Verifica que la lista contiene exactamente 3 elementos
                .contains(response1, response2, response3);                                       // Verifica que la lista contiene estos 3 objetos específicos

        verify(this.employeeService).getAllEmployees(null, null);          // Verifica que el servicio mockeado fue llamado con estos parámetros
    }

    @Test
    void shouldReturnFilteredEmployees_whenPositionProvided() {
        // given
        String position = "Developer";
        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDeveloper(1L, true));
        EmployeeResponse response2 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDeveloper(2L, false));

        when(this.employeeService.getAllEmployees(position, null))
                .thenReturn(Flux.just(response1, response2));

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
                .contains(response1, response2);

        verify(this.employeeService).getAllEmployees(position, null);
    }

    @Test
    void shouldReturnFilteredEmployees_whenFullTimeProvided() {
        // given
        Boolean fullTime = true;
        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDeveloper(1L, true));
        EmployeeResponse response2 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDesigner(2L, true));

        when(this.employeeService.getAllEmployees(null, fullTime))
                .thenReturn(Flux.just(response1, response2));

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
                .hasSize(2)
                .contains(response1, response2);

        verify(this.employeeService).getAllEmployees(null, fullTime);
    }

    @Test
    void shouldReturnFilteredEmployees_whenBothFiltersProvided() {
        // given
        String position = "Developer";
        Boolean fullTime = true;
        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(EmployeeFixture.createDeveloper(1L, true));

        when(this.employeeService.getAllEmployees(position, fullTime))
                .thenReturn(Flux.just(response1));

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
                .hasSize(1)
                .contains(response1);

        verify(this.employeeService).getAllEmployees(position, fullTime);
    }
}
