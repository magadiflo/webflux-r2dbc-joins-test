package dev.magadiflo.app.unit.controller;

import dev.magadiflo.app.controller.EmployeeController;
import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.fixtures.EmployeeFixture;
import dev.magadiflo.app.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
                .accept(MediaType.TEXT_EVENT_STREAM)    // Tipo de contenido esperado en la respuesta (Accept header)
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

    @Test
    void shouldReturnEmptyStream_whenNoEmployeesFound() {
        // given
        when(this.employeeService.getAllEmployees(null, null))
                .thenReturn(Flux.empty());

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBodyList(EmployeeResponse.class)
                .hasSize(0);

        verify(this.employeeService).getAllEmployees(null, null);
    }

    // ===== GET /api/v1/employees/{employeeId} - findEmployee =====
    @Test
    void shouldReturnEmployee_whenValidIdProvided() {
        // given
        Long employeeId = 1L;
        EmployeeResponse employeeResponse = EmployeeFixture
                .toEmployeeResponse(EmployeeFixture.createDefaultEmployee(employeeId));

        when(this.employeeService.showEmployee(employeeId))
                .thenReturn(Mono.just(employeeResponse));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/{employeeId}", employeeId)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EmployeeResponse.class)
                .isEqualTo(employeeResponse);

        verify(this.employeeService).showEmployee(employeeId);
    }

    @Test
    void shouldReturn404_whenEmployeeNotFound() {
        // given
        Long nonExistentId = 999L;
        when(this.employeeService.showEmployee(nonExistentId))
                .thenReturn(Mono.error(() -> new EmployeeNotFoundException(nonExistentId)));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/{employeeId}", nonExistentId)
                .exchange();

        // then
        response.expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Empleado no encontrado")
                .jsonPath("$.detail").isEqualTo("El empleado con id [999] no fue encontrado");

        verify(this.employeeService).showEmployee(nonExistentId);
    }

    // ===== POST /api/v1/employees - saveEmployee =====
    @Test
    void shouldCreateEmployee_whenValidRequestProvided() {
        // given
        EmployeeRequest request = EmployeeFixture.createDefaultRequest();
        EmployeeResponse employeeResponse = new EmployeeResponse(
                1L,
                request.firstName(),
                request.lastName(),
                request.position(),
                request.fullTime()
        );

        when(this.employeeService.createEmployee(request))
                .thenReturn(Mono.just(employeeResponse));

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
                .isEqualTo(employeeResponse);

        verify(this.employeeService).createEmployee(request);
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


        // No debe llamarse al servicio si la validación falla
        verifyNoInteractions(this.employeeService);
    }

    // ===== PUT /api/v1/employees/{employeeId} - updateEmployee =====
    @Test
    void shouldUpdateEmployee_whenValidDataProvided() {
        // given
        Long employeeId = 1L;
        EmployeeRequest request = EmployeeFixture.createDefaultRequest();
        EmployeeResponse employeeResponse = new EmployeeResponse(
                employeeId,
                request.firstName(),
                request.lastName(),
                request.position(),
                request.fullTime()
        );

        when(this.employeeService.updateEmployee(employeeId, request))
                .thenReturn(Mono.just(employeeResponse));

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
                .expectBody(EmployeeResponse.class)
                .isEqualTo(employeeResponse);

        verify(this.employeeService).updateEmployee(employeeId, request);
    }

    @Test
    void shouldReturn404_whenUpdatingNonExistentEmployee() {
        // given
        Long nonExistentId = 999L;
        EmployeeRequest request = EmployeeFixture.createUpdateRequest();

        when(this.employeeService.updateEmployee(eq(nonExistentId), any(EmployeeRequest.class)))
                .thenReturn(Mono.error(() -> new EmployeeNotFoundException(nonExistentId)));

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

        // Usamos eq(nonExistentId) y any(EmployeeRequest.class) porque:
        // - Queremos verificar que el servicio se invoca con el ID específico (eq asegura coincidencia exacta).
        // - No nos importa la instancia exacta del EmployeeRequest, solo que sea de ese tipo (any evita que el test
        // falle si el controlador crea una nueva instancia con los mismos datos).
        verify(this.employeeService).updateEmployee(eq(nonExistentId), any(EmployeeRequest.class));
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

        verifyNoInteractions(this.employeeService);
    }

    // ===== DELETE /api/v1/employees/{employeeId} - deleteEmployee =====
    @Test
    void shouldDeleteEmployee_whenValidIdProvided() {
        // given
        Long employeeId = 1L;
        when(this.employeeService.deleteEmployee(employeeId))
                .thenReturn(Mono.empty());

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.delete()
                .uri("/api/v1/employees/{employeeId}", employeeId)
                .exchange();

        // then
        response.expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(this.employeeService).deleteEmployee(employeeId);
    }
}

