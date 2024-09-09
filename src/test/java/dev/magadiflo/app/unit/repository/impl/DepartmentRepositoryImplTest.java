package dev.magadiflo.app.unit.repository.impl;

import dev.magadiflo.app.model.entity.Department;
import dev.magadiflo.app.repository.EmployeeRepository;
import dev.magadiflo.app.repository.impl.DepartmentRepositoryImpl;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentRepositoryImplTest {

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private DatabaseClient.GenericExecuteSpec genericExecuteSpec;

    @Mock
    private FetchSpec<Map<String, Object>> fetchSpec;

    @Mock
    private RowsFetchSpec<Department> rowsFetchSpec;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentRepositoryImpl departmentRepository;

    @Test
    void shouldReturnFluxOfDepartments_whenDataExists() {
        // given
        Map<String, Object> row1 = Map.of("d_id", 1L, "d_name", "Legal");
        Map<String, Object> row2 = Map.of("d_id", 2L, "d_name", "Ventas");
        Flux<Map<String, Object>> mockResult = Flux.just(row1, row2);
        when(this.databaseClient.sql(anyString())).thenReturn(this.genericExecuteSpec);
        when(this.genericExecuteSpec.fetch()).thenReturn(this.fetchSpec);
        when(this.fetchSpec.all()).thenReturn(mockResult);

        // when
        Flux<Department> result = this.departmentRepository.findAll();

        // then
        StepVerifier.create(result)
                .consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getId()).isEqualTo(1L);
                    assertThat(departmentDB.getName()).isEqualTo("Legal");
                }).consumeNextWith(departmentDB -> {
                    assertThat(departmentDB.getId()).isEqualTo(2L);
                    assertThat(departmentDB.getName()).isEqualTo("Ventas");
                })
                .verifyComplete();
        verify(this.databaseClient).sql(anyString());
        verify(this.genericExecuteSpec).fetch();
        verify(this.fetchSpec).all();
    }

    @Test
    void shouldReturnDepartment_whenValidIdIsProvided() {
        // given
        Long validDepartmentId = 1L;
        Department expectedDepartment = Department.builder()
                .id(validDepartmentId)
                .name("HR")
                .build();

        when(this.databaseClient.sql(anyString())).thenReturn(this.genericExecuteSpec);
        when(this.genericExecuteSpec.bind(anyString(), any())).thenReturn(this.genericExecuteSpec);
        when(this.genericExecuteSpec.map(Mockito.<BiFunction<Row, RowMetadata, Department>>any())).thenReturn(this.rowsFetchSpec);
        when(this.rowsFetchSpec.first()).thenReturn(Mono.just(expectedDepartment));

        // when
        Mono<Department> result = this.departmentRepository.findById(validDepartmentId);

        // then
        StepVerifier.create(result)
                .expectNext(expectedDepartment)
                .verifyComplete();
        verify(this.databaseClient).sql(anyString());
        verify(this.genericExecuteSpec).bind(eq("departmentId"), eq(validDepartmentId));
        verify(this.genericExecuteSpec).bind(anyString(), any());
        verify(this.genericExecuteSpec).map(Mockito.<BiFunction<Row, RowMetadata, Department>>any());
        verify(this.rowsFetchSpec).first();
    }
}