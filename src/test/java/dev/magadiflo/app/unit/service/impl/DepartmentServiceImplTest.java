package dev.magadiflo.app.unit.service.impl;

import dev.magadiflo.app.dao.DepartmentDao;
import dev.magadiflo.app.exception.DepartmentAlreadyExistsException;
import dev.magadiflo.app.model.dto.CreateDepartmentRequest;
import dev.magadiflo.app.model.entity.Department;
import dev.magadiflo.app.service.impl.DepartmentServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentDao departmentDao;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void shouldReturnDepartments_whenDataExists() {
        // given
        Flux<Department> departments = Flux.just(
                new Department(1L, "HR", null, List.of()),
                new Department(2L, "Sales", null, List.of()),
                new Department(3L, "Business", null, List.of())
        );
        when(this.departmentDao.findAll()).thenReturn(departments);

        // when
        Flux<Department> result = this.departmentService.getAllDepartments();

        // then
        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
        verify(this.departmentDao).findAll();
    }

    @Test
    void shouldThrowDepartmentAlreadyExistsException_whenDepartmentAlreadyExists() {
        // given
        when(this.departmentDao.findByName(anyString())).thenReturn(Mono.just(new Department(1L, "HR", null, List.of())));

        // when
        Mono<Department> result = this.departmentService.createDepartment(new CreateDepartmentRequest("HR"));

        // then
        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(DepartmentAlreadyExistsException.class);
                    assertThat(throwable.getMessage()).isEqualTo("El departamento con nombre %s ya existe".formatted("HR"));
                })
                .verify();
        verify(this.departmentDao, never()).save(any(Department.class));
    }
}