package dev.magadiflo.app.unit.service;

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
import static org.mockito.ArgumentMatchers.any;
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
    void shouldRetrieveAllEmployeesWhenPositionAndIsFullTimeAreNull() {
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
        verify(this.employeeRepository, never()).findByPosition(any());
        verify(this.employeeRepository, never()).findByFullTime(any());
        verify(this.employeeRepository, never()).findByPositionAndFullTime(any(), any());
    }

    @Test
    void shouldFindEmployeeById() {
        // given
        Long employeeId = 1L;
        Employee employee = EmployeeFixture.createDefaultEmployee();
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
}
