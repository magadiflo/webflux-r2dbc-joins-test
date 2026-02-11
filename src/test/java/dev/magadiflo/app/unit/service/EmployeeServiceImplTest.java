package dev.magadiflo.app.unit.service;

import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Employee;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.fixtures.EmployeeFixture;
import dev.magadiflo.app.mapper.EmployeeMapper;
import dev.magadiflo.app.repository.EmployeeRepository;
import dev.magadiflo.app.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeMapper employeeMapper;
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void shouldReturnAllEmployeesWhenNoFiltersProvided() {
        // given
        Employee employee1 = EmployeeFixture.createDeveloper(1L, true);
        Employee employee2 = EmployeeFixture.createDesigner(2L, false);
        Employee employee3 = EmployeeFixture.createDeveloper(3L, false);

        List<Employee> employeeList = List.of(employee1, employee2, employee3);

        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(employee1);
        EmployeeResponse response2 = EmployeeFixture.toEmployeeResponse(employee2);
        EmployeeResponse response3 = EmployeeFixture.toEmployeeResponse(employee3);

        when(this.employeeRepository.findAll())
                .thenReturn(Flux.fromIterable(employeeList));
        when(this.employeeMapper.toEmployeeResponse(employee1)).thenReturn(response1);
        when(this.employeeMapper.toEmployeeResponse(employee2)).thenReturn(response2);
        when(this.employeeMapper.toEmployeeResponse(employee3)).thenReturn(response3);

        // when
        Flux<EmployeeResponse> result = this.employeeService.getAllEmployees(null, null);

        // then
        StepVerifier.create(result)
                .expectNext(response1, response2, response3)
                .verifyComplete();

        verify(this.employeeRepository).findAll();
        verify(this.employeeRepository, never()).findByPosition(anyString());
        verify(this.employeeRepository, never()).findByFullTime(anyBoolean());
        verify(this.employeeRepository, never()).findByPositionAndFullTime(anyString(), anyBoolean());
    }

    @Test
    void shouldReturnEmployeesWhenFilteredByPositionOnly() {
        // given
        String position = "Developer";
        Employee employee1 = EmployeeFixture.createDeveloper(1L, true);
        Employee employee2 = EmployeeFixture.createDeveloper(2L, false);

        List<Employee> developers = List.of(employee1, employee2);

        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(employee1);
        EmployeeResponse response2 = EmployeeFixture.toEmployeeResponse(employee2);

        when(this.employeeRepository.findByPosition(position))
                .thenReturn(Flux.fromIterable(developers));
        when(this.employeeMapper.toEmployeeResponse(employee1)).thenReturn(response1);
        when(this.employeeMapper.toEmployeeResponse(employee2)).thenReturn(response2);

        // when
        Flux<EmployeeResponse> result = this.employeeService.getAllEmployees(position, null);

        // then
        StepVerifier.create(result)
                .expectNext(response1, response2)
                .verifyComplete();

        verify(this.employeeRepository).findByPosition(position);
        verify(this.employeeRepository, never()).findAll();
        verify(this.employeeRepository, never()).findByFullTime(anyBoolean());
        verify(this.employeeRepository, never()).findByPositionAndFullTime(anyString(), anyBoolean());
    }

    @Test
    void shouldReturnEmployeesWhenFilteredByFullTimeOnly() {
        // given
        Boolean isFullTime = true;
        Employee employee1 = EmployeeFixture.createDeveloper(1L, true);
        Employee employee2 = EmployeeFixture.createDesigner(2L, true);

        List<Employee> fullTimeEmployees = List.of(employee1, employee2);

        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(employee1);
        EmployeeResponse response2 = EmployeeFixture.toEmployeeResponse(employee2);

        when(this.employeeRepository.findByFullTime(isFullTime))
                .thenReturn(Flux.fromIterable(fullTimeEmployees));
        when(this.employeeMapper.toEmployeeResponse(employee1)).thenReturn(response1);
        when(this.employeeMapper.toEmployeeResponse(employee2)).thenReturn(response2);

        // when
        Flux<EmployeeResponse> result = this.employeeService.getAllEmployees(null, isFullTime);

        // then
        StepVerifier.create(result)
                .expectNext(response1, response2)
                .verifyComplete();

        verify(this.employeeRepository).findByFullTime(isFullTime);
        verify(this.employeeRepository, never()).findAll();
        verify(this.employeeRepository, never()).findByPosition(anyString());
        verify(this.employeeRepository, never()).findByPositionAndFullTime(anyString(), anyBoolean());
    }

    @Test
    void shouldReturnEmployeesWhenFilteredByPositionAndFullTime() {
        // given
        String position = "Developer";
        Boolean isFullTime = true;
        Employee employee1 = EmployeeFixture.createDeveloper(1L, true);
        Employee employee2 = EmployeeFixture.createDeveloper(2L, true);

        List<Employee> filteredEmployees = List.of(employee1, employee2);

        EmployeeResponse response1 = EmployeeFixture.toEmployeeResponse(employee1);
        EmployeeResponse response2 = EmployeeFixture.toEmployeeResponse(employee2);

        when(this.employeeRepository.findByPositionAndFullTime(position, isFullTime))
                .thenReturn(Flux.fromIterable(filteredEmployees));
        when(this.employeeMapper.toEmployeeResponse(employee1)).thenReturn(response1);
        when(this.employeeMapper.toEmployeeResponse(employee2)).thenReturn(response2);

        // when
        Flux<EmployeeResponse> result = this.employeeService.getAllEmployees(position, isFullTime);

        // then
        StepVerifier.create(result)
                .expectNext(response1, response2)
                .verifyComplete();

        verify(this.employeeRepository).findByPositionAndFullTime(position, isFullTime);
        verify(this.employeeRepository, never()).findAll();
        verify(this.employeeRepository, never()).findByPosition(anyString());
        verify(this.employeeRepository, never()).findByFullTime(anyBoolean());
    }

    @Test
    void shouldReturnEmptyFluxWhenNoEmployeesFound() {
        // given
        when(this.employeeRepository.findAll()).thenReturn(Flux.empty());

        // when
        Flux<EmployeeResponse> result = this.employeeService.getAllEmployees(null, null);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        verify(this.employeeRepository).findAll();
        verifyNoInteractions(this.employeeMapper);
    }

    @Test
    void shouldFindEmployeeById() {
        // given
        Long employeeId = 1L;
        Employee employee = EmployeeFixture.createDefaultEmployee(employeeId);
        EmployeeResponse employeeResponse = EmployeeFixture.toEmployeeResponse(employee);

        when(this.employeeRepository.findById(employeeId))
                .thenReturn(Mono.just(employee));
        when(this.employeeMapper.toEmployeeResponse(employee))
                .thenReturn(employeeResponse);

        // when
        Mono<EmployeeResponse> result = this.employeeService.showEmployee(employeeId);

        // then
        StepVerifier.create(result)
                .expectNext(employeeResponse)
                .verifyComplete();

        verify(this.employeeRepository).findById(employeeId);
        verify(this.employeeMapper).toEmployeeResponse(employee);
    }

    @Test
    void shouldThrowEmployeeNotFoundExceptionWhenEmployeeDoesNotExist() {
        // given
        Long nonExistentId = 999L;
        when(this.employeeRepository.findById(nonExistentId))
                .thenReturn(Mono.empty());

        // when
        Mono<EmployeeResponse> result = this.employeeService.showEmployee(nonExistentId);

        // then
        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable)
                            .isExactlyInstanceOf(EmployeeNotFoundException.class)
                            .hasMessage("El empleado con id [%d] no fue encontrado".formatted(nonExistentId));
                })
                .verify();

        verify(this.employeeRepository).findById(nonExistentId);
        verifyNoInteractions(this.employeeMapper);
    }

    /**
     * Sobre el thenAnswer
     * <p>
     * 1. invocation.getArgument(0) captura el primer argumento (índice 0) del método mockeado.
     * 2. Si hubiera múltiples argumentos: getArgument(0), getArgument(1), etc.
     * 3. Puedes modificar el objeto capturado (como agregarle un ID).
     * 4. Retornas lo que necesites (el objeto modificado, una transformación, etc.).
     */
    @Test
    void shouldCreateEmployee() {
        // given
        EmployeeRequest request = EmployeeFixture.createDefaultRequest();

        // Mock del repository.save():
        // Captura el Employee que se intenta guardar, le asigna un ID (simulando auto-generación de BD),
        // y retorna el Employee guardado envuelto en Mono
        when(this.employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> {
                    Employee savedEmployee = invocation.getArgument(0); // Captura el argumento en posición 0
                    savedEmployee.setId(1L);  // Simula la asignación de ID por la BD
                    return Mono.just(savedEmployee);
                });

        // Mock del mapper.toEmployeeResponse():
        // Captura el Employee guardado y lo transforma a EmployeeResponse usando el fixture
        when(this.employeeMapper.toEmployeeResponse(any(Employee.class)))
                .thenAnswer(invocation -> {
                    Employee savedEmployee = invocation.getArgument(0); // Captura el Employee guardado
                    return EmployeeFixture.toEmployeeResponse(savedEmployee); // Transforma a Response
                });

        // when
        Mono<EmployeeResponse> result = this.employeeService.createEmployee(request);

        // then
        StepVerifier.create(result)
                .assertNext(employeeResponse -> {
                    assertThat(employeeResponse.id()).isNotNull(); // Verifica que se asignó el ID
                    assertThat(employeeResponse.firstName()).isEqualTo(request.firstName());
                    assertThat(employeeResponse.lastName()).isEqualTo(request.lastName());
                    assertThat(employeeResponse.position()).isEqualTo(request.position());
                    assertThat(employeeResponse.fullTime()).isEqualTo(request.fullTime());
                })
                .verifyComplete();
        verify(this.employeeRepository).save(any(Employee.class));
        verify(this.employeeMapper).toEmployeeResponse(any(Employee.class));
    }

    @Test
    void shouldUpdateEmployee() {
        // given
        Long employeeId = 1L;
        EmployeeRequest request = EmployeeFixture.createUpdateRequest();
        Employee existingEmployee = EmployeeFixture.createDefaultEmployee(employeeId);

        // Mock: Retorna el employee existente cuando se busca por ID
        when(this.employeeRepository.findById(employeeId))
                .thenReturn(Mono.just(existingEmployee));

        // Mock: El save() retorna el mismo objeto que recibe (simula comportamiento real de BD).
        // El servicio ya modificó el objeto antes de llamar save(), por eso retornamos el argumento capturado
        when(this.employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Mock: Transforma el Employee actualizado a EmployeeResponse usando el fixture
        when(this.employeeMapper.toEmployeeResponse(any(Employee.class)))
                .thenAnswer(invocation -> {
                    Employee updatedEmployee = invocation.getArgument(0);
                    return EmployeeFixture.toEmployeeResponse(updatedEmployee);
                });

        // when
        Mono<EmployeeResponse> result = this.employeeService.updateEmployee(employeeId, request);

        // then
        StepVerifier.create(result)
                .assertNext(employeeResponse -> {
                    assertThat(employeeResponse.id())
                            .isNotNull()
                            .isEqualTo(employeeId);
                    assertThat(employeeResponse.firstName()).isEqualTo(request.firstName());
                    assertThat(employeeResponse.lastName()).isEqualTo(request.lastName());
                    assertThat(employeeResponse.position()).isEqualTo(request.position());
                    assertThat(employeeResponse.fullTime()).isEqualTo(request.fullTime());
                })
                .verifyComplete();
        verify(this.employeeRepository).findById(employeeId);
        verify(this.employeeRepository).save(any(Employee.class));
        verify(this.employeeMapper).toEmployeeResponse(any(Employee.class));
    }

    @Test
    void shouldThrowEmployeeNotFoundExceptionWhenUpdatingNonExistentEmployee() {
        // given
        Long nonExistentId = 999L;
        EmployeeRequest updateRequest = EmployeeFixture.createUpdateRequest();

        when(this.employeeRepository.findById(nonExistentId))
                .thenReturn(Mono.empty());

        // when
        Mono<EmployeeResponse> result = this.employeeService.updateEmployee(nonExistentId, updateRequest);

        // then
        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable)
                            .isExactlyInstanceOf(EmployeeNotFoundException.class)
                            .hasMessage("El empleado con id [%d] no fue encontrado".formatted(nonExistentId));
                })
                .verify();

        verify(this.employeeRepository).findById(nonExistentId);
        verify(this.employeeRepository, never()).save(any(Employee.class));
        verifyNoInteractions(this.employeeMapper);
    }
}
