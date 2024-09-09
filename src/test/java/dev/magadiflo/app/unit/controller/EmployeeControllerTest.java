package dev.magadiflo.app.unit.controller;

import dev.magadiflo.app.controller.EmployeeController;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.handler.ErrorResponse;
import dev.magadiflo.app.model.dto.CreateEmployeeRequest;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@WebFluxTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void shouldFindAllEmployees_whenDataExits() {
        // given
        Employee e1 = new Employee(1L, "Martín", "Díaz", "Desarrollador", true);
        Employee e2 = new Employee(2L, "Betania", "Velez", "Abogada", false);
        when(this.employeeService.getAllEmployees(isNull(), isNull())).thenReturn(Flux.just(e1, e2));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees")
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBodyList(Employee.class)
                .hasSize(2)
                .consumeWith(listEntityExchangeResult -> {
                    List<Employee> responseBody = listEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotEmpty();
                    assertThat(responseBody.get(0).getId()).isEqualTo(e1.getId());
                    assertThat(responseBody.get(1).getId()).isEqualTo(e2.getId());
                });
        verify(this.employeeService).getAllEmployees(isNull(), isNull());
    }

    @Test
    void shouldReturnEmployee_whenValidIdIsProvided() {
        // given
        Employee e1 = new Employee(1L, "Martín", "Díaz", "Desarrollador", true);
        when(this.employeeService.showEmployee(anyLong())).thenReturn(Mono.just(e1));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/{employeeId}", 1)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBody(Employee.class)
                .consumeWith(employeeEntityExchangeResult -> {
                    Employee responseBody = employeeEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody.getId()).isEqualTo(e1.getId());
                    assertThat(responseBody.getFirstName()).isEqualTo(e1.getFirstName());
                    assertThat(responseBody.getLastName()).isEqualTo(e1.getLastName());
                    assertThat(responseBody.isFullTime()).isEqualTo(e1.isFullTime());
                });
        verify(this.employeeService).showEmployee(anyLong());
    }

    @Test
    void shouldReturnNotFoundMessage_whenInvalidIdIsProvided() {
        // given
        when(this.employeeService.showEmployee(anyLong())).thenThrow(new EmployeeNotFoundException(1L));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.get()
                .uri("/api/v1/employees/{employeeId}", 1)
                .exchange();

        // then
        response.expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .consumeWith(employeeEntityExchangeResult -> {
                    ErrorResponse responseBody = employeeEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody.errors().get("message")).isEqualTo("El empleado con id 1 no fue encontrado");
                });
        verify(this.employeeService).showEmployee(anyLong());
    }

    @Test
    void shouldSaveEmployee_whenValidEmployeeIsProvided() {
        // given
        CreateEmployeeRequest request = new CreateEmployeeRequest("Milu", "Jara", "Teacher", false);
        Employee savedEmployee = new Employee(1L, "Milu", "Jara", "Teacher", false);
        when(this.employeeService.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(Mono.just(savedEmployee));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.post()
                .uri("/api/v1/employees")
                .bodyValue(request)
                .exchange();

        // then
        response.expectStatus().isCreated()
                .expectBody(Employee.class)
                .consumeWith(employeeEntityExchangeResult -> {
                    Employee responseBody = employeeEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody.getId()).isEqualTo(1L);
                    assertThat(responseBody.getFirstName()).isEqualTo(request.firstName());
                    assertThat(responseBody.getLastName()).isEqualTo(request.lastName());
                    assertThat(responseBody.isFullTime()).isEqualTo(request.isFullTime());
                });
        verify(this.employeeService).createEmployee(any(CreateEmployeeRequest.class));
    }

    @Test
    void shouldUpdateEmployee_whenValidIdIsProvided() {
        // given
        Employee updatedEmployee = new Employee(1L, "Martín", "Díaz", "Desarrollador Senior", true);

        when(this.employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(Mono.just(updatedEmployee));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.put()
                .uri("/api/v1/employees/{employeeId}", 1)
                .bodyValue(updatedEmployee)
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBody(Employee.class)
                .consumeWith(employeeEntityExchangeResult -> {
                    Employee responseBody = employeeEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody.getId()).isEqualTo(updatedEmployee.getId());
                    assertThat(responseBody.getFirstName()).isEqualTo(updatedEmployee.getFirstName());
                    assertThat(responseBody.getLastName()).isEqualTo(updatedEmployee.getLastName());
                    assertThat(responseBody.isFullTime()).isEqualTo(updatedEmployee.isFullTime());
                });
        verify(this.employeeService).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    void shouldDeleteEmployee_whenValidIdIsProvided() {
        // given
        when(this.employeeService.deleteEmployee(anyLong())).thenReturn(Mono.empty());

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.delete()
                .uri("/api/v1/employees/{employeeId}", 1)
                .exchange();

        // then
        response.expectStatus().isNoContent();
        verify(this.employeeService).deleteEmployee(anyLong());
    }

    @Test
    void shouldReturnNotFoundMessageToDelete_whenInvalidIdIsProvided() {
        // given
        when(this.employeeService.deleteEmployee(anyLong())).thenThrow(new EmployeeNotFoundException(100L));

        // when
        WebTestClient.ResponseSpec response = this.webTestClient.delete()
                .uri("/api/v1/employees/{employeeId}", 100)
                .exchange();

        // then
        response.expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .consumeWith(errorResponseEntityExchangeResult -> {
                    ErrorResponse responseBody = errorResponseEntityExchangeResult.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody.errors().get("message")).isEqualTo("El empleado con id 100 no fue encontrado");
                });
        verify(this.employeeService).deleteEmployee(anyLong());
    }
}