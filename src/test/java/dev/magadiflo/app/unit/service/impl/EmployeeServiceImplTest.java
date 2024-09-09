package dev.magadiflo.app.unit.service.impl;

import dev.magadiflo.app.exception.EmployeeNotFoundException;
import dev.magadiflo.app.model.dto.CreateEmployeeRequest;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.repository.EmployeeRepository;
import dev.magadiflo.app.service.impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private List<Employee> employees;

    @BeforeEach
    void setUp() {
        this.employees = List.of(
                new Employee(1L, "Martín", "Díaz", "Desarrollador", true),
                new Employee(2L, "Betania", "Velez", "Abogada", false),
                new Employee(3L, "Indira", "Sánchez", "Enfermera", true)
        );
    }

    @Test
    void shouldReturnEmployees_whenDataExists() {
        // given
        when(this.employeeRepository.findAll()).thenReturn(Flux.fromIterable(employees));

        // when
        Flux<Employee> result = this.employeeService.getAllEmployees(null, null);

        // then
        StepVerifier.create(result)
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getId()).isEqualTo(1L);
                    assertThat(employeeDB.getFirstName()).isEqualTo("Martín");
                })
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getId()).isEqualTo(2L);
                    assertThat(employeeDB.getFirstName()).isEqualTo("Betania");
                })
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getId()).isEqualTo(3L);
                    assertThat(employeeDB.getFirstName()).isEqualTo("Indira");
                })
                .verifyComplete();
        verify(this.employeeRepository).findAll();
        verify(this.employeeRepository, never()).findAllByPositionAndFullTime(anyString(), anyBoolean());
        verify(this.employeeRepository, never()).findAllByPosition(anyString());
        verify(this.employeeRepository, never()).findAllByFullTime(anyBoolean());

    }

    @Test
    void shouldReturnEmployeeByPosition_whenDataExists() {
        // given
        when(this.employeeRepository.findAllByPosition(anyString())).thenReturn(Flux.just(this.employees.get(0)));

        // when
        Flux<Employee> result = this.employeeService.getAllEmployees("Desarrollador", null);

        // then
        StepVerifier.create(result)
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getId()).isEqualTo(1L);
                    assertThat(employeeDB.getFirstName()).isEqualTo("Martín");
                })
                .verifyComplete();
        verify(this.employeeRepository, never()).findAll();
        verify(this.employeeRepository, never()).findAllByPositionAndFullTime(anyString(), anyBoolean());
        verify(this.employeeRepository).findAllByPosition(anyString());
        verify(this.employeeRepository, never()).findAllByFullTime(anyBoolean());
    }

    @Test
    void shouldReturnEmployeeByFullTime_whenDataExists() {
        // given
        Flux<Employee> employeeFlux = Flux.just(this.employees.get(0), this.employees.get(2));
        when(this.employeeRepository.findAllByFullTime(anyBoolean())).thenReturn(employeeFlux);

        // when
        Flux<Employee> result = this.employeeService.getAllEmployees(null, true);

        // then
        StepVerifier.create(result)
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getId()).isEqualTo(1L);
                    assertThat(employeeDB.getFirstName()).isEqualTo("Martín");
                    assertThat(employeeDB.isFullTime()).isTrue();
                })
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getId()).isEqualTo(3L);
                    assertThat(employeeDB.getFirstName()).isEqualTo("Indira");
                    assertThat(employeeDB.isFullTime()).isTrue();
                })
                .verifyComplete();
        verify(this.employeeRepository, never()).findAll();
        verify(this.employeeRepository, never()).findAllByPositionAndFullTime(anyString(), anyBoolean());
        verify(this.employeeRepository, never()).findAllByPosition(anyString());
        verify(this.employeeRepository).findAllByFullTime(anyBoolean());
    }

    @Test
    void shouldReturnEmployeeByPositionAndFullTime_whenDataExists() {
        // given
        when(this.employeeRepository.findAllByPositionAndFullTime(anyString(), anyBoolean())).thenReturn(Flux.just(this.employees.get(1)));

        // when
        Flux<Employee> result = this.employeeService.getAllEmployees("Abogada", true);

        // then
        StepVerifier.create(result)
                .consumeNextWith(employeeDB -> {
                    assertThat(employeeDB.getId()).isEqualTo(2L);
                    assertThat(employeeDB.getFirstName()).isEqualTo("Betania");
                    assertThat(employeeDB.getPosition()).isEqualTo("Abogada");
                    assertThat(employeeDB.isFullTime()).isFalse();
                })
                .verifyComplete();
        verify(this.employeeRepository, never()).findAll();
        verify(this.employeeRepository).findAllByPositionAndFullTime(anyString(), anyBoolean());
        verify(this.employeeRepository, never()).findAllByPosition(anyString());
        verify(this.employeeRepository, never()).findAllByFullTime(anyBoolean());
    }

    @Test
    void shouldReturnEmployee_whenValidIdIsProvided() {
        // given
        when(this.employeeRepository.findById(anyLong())).thenReturn(Mono.just(this.employees.get(0)));

        // when
        Mono<Employee> result = this.employeeService.showEmployee(1L);

        // then
        StepVerifier.create(result)
                .expectNext(this.employees.get(0))
                .verifyComplete();
        verify(this.employeeRepository).findById(anyLong());
    }

    @Test
    void shouldThrowEmployeeNotFoundException_whenInvalidIdIsProvided() {
        // given
        when(this.employeeRepository.findById(anyLong())).thenReturn(Mono.empty());

        // when
        Mono<Employee> result = this.employeeService.showEmployee(100L);

        // then
        StepVerifier.create(result)
                .expectError(EmployeeNotFoundException.class)
                .verify();
        verify(this.employeeRepository).findById(anyLong());
    }

    @Test
    void shouldSaveEmployee_whenValidEmployeeIsProvided() {
        // given
        Employee employeeToSave = Employee.builder()
                .firstName("Danny")
                .lastName("Castro")
                .position("Doctor")
                .fullTime(true)
                .build();
        when(this.employeeRepository.save(any(Employee.class))).thenReturn(Mono.just(employeeToSave));

        // when
        Mono<Employee> result = this.employeeService.createEmployee(new CreateEmployeeRequest("Danny", "Castro", "Doctor", true));

        // then
        StepVerifier.create(result)
                .consumeNextWith(employeeDB -> assertThat(employeeDB).isEqualTo(employeeToSave))
                .verifyComplete();
        verify(this.employeeRepository).save(any(Employee.class));
    }

    @Test
    void shouldUpdateEmployee_whenValidEmployeeIsProvided() {
        // given
        Employee employee = Employee.builder()
                .id(1L)
                .firstName("Lizbeth")
                .lastName("Gonzales")
                .position("Teacher")
                .fullTime(true)
                .build();
        when(this.employeeRepository.findById(anyLong())).thenReturn(Mono.just(employee));
        employee.setPosition("Docente");
        employee.setFullTime(false);
        when(this.employeeRepository.save(any(Employee.class))).thenReturn(Mono.just(employee));

        // when

        Mono<Employee> result = this.employeeService.updateEmployee(1L, employee);

        // then
        StepVerifier.create(result)
                .consumeNextWith(employeeDB -> assertThat(employeeDB).isEqualTo(employee))
                .verifyComplete();
        verify(this.employeeRepository).findById(anyLong());
        verify(this.employeeRepository).save(any(Employee.class));
    }

    @Test
    void shouldThrowEmployeeNotFoundException_whenInvalidIdIsProvidedForUpdated() {
        // given
        when(this.employeeRepository.findById(anyLong())).thenReturn(Mono.empty());

        // when
        Mono<Employee> result = this.employeeService.updateEmployee(100L, this.employees.get(0));

        // then
        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(EmployeeNotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("El empleado con id %d no fue encontrado".formatted(100));
                })
                .verify();
        verify(this.employeeRepository).findById(anyLong());
        verify(this.employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void shouldDeleteEmployee_whenValidIdIsProvided() {
        // given
        when(this.employeeRepository.findById(anyLong())).thenReturn(Mono.just(this.employees.get(0)));
        when(this.employeeRepository.delete(any(Employee.class))).thenReturn(Mono.empty());

        // when
        Mono<Void> result = this.employeeService.deleteEmployee(1L);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        verify(this.employeeRepository).findById(anyLong());
        verify(this.employeeRepository).delete(any(Employee.class));
    }

    @Test
    void shouldReturnEmployeeNotFoundException_whenInvalidIdIsProvided() {
        // given
        when(this.employeeRepository.findById(anyLong())).thenReturn(Mono.empty());

        // when
        Mono<Void> result = this.employeeService.deleteEmployee(100L);

        // then
        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(EmployeeNotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("El empleado con id %d no fue encontrado".formatted(100));
                })
                .verify();
        verify(this.employeeRepository).findById(anyLong());
        verify(this.employeeRepository, never()).delete(any(Employee.class));
    }
}