package dev.magadiflo.app.controller;

import dev.magadiflo.app.dto.DepartmentRequest;
import dev.magadiflo.app.dto.DepartmentResponse;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Department;
import dev.magadiflo.app.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<DepartmentResponse>>> findAllDepartments() {
        Flux<DepartmentResponse> departmentResponseFlux = this.departmentService.getAllDepartments()
                .doOnNext(departmentResponse -> log.info("{}", departmentResponse));
        return Mono.just(ResponseEntity.ok(departmentResponseFlux));
    }

    @GetMapping(path = "/{departmentId}/stream-employees", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<EmployeeResponse>>> getEmployeesFromDepartment(@PathVariable Long departmentId,
                                                                                   @RequestParam(required = false) Boolean fullTime) {
        Flux<EmployeeResponse> employeeResponseFlux = this.departmentService.getEmployeesFromDepartment(departmentId, fullTime)
                .doOnNext(employeeResponse -> log.info("{}", employeeResponse));
        return Mono.just(ResponseEntity.ok(employeeResponseFlux));
    }

    @GetMapping(path = "/{departmentId}")
    public Mono<ResponseEntity<DepartmentResponse>> findDepartment(@PathVariable Long departmentId) {
        return this.departmentService.showDepartment(departmentId)
                .doOnNext(departmentResponse -> log.info("{}", departmentResponse))
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/{departmentId}/manager-employees")
    public Mono<ResponseEntity<DepartmentResponse>> findWithManagerAndEmployees(@PathVariable Long departmentId) {
        return this.departmentService.showDepartmentWithManagerAndEmployees(departmentId)
                .doOnNext(departmentResponse -> log.info("{}", departmentResponse))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<DepartmentResponse>> saveDepartment(@Valid @RequestBody DepartmentRequest request) {
        return this.departmentService.createDepartment(request)
                .doOnNext(departmentResponse -> log.info("{}", departmentResponse))
                .map(departmentResponse ->
                        ResponseEntity.status(HttpStatus.CREATED).body(departmentResponse)
                );
    }

    @PutMapping(path = "/{departmentId}")
    public Mono<ResponseEntity<DepartmentResponse>> updateDepartment(@PathVariable Long departmentId,
                                                                     @RequestBody Department department) {
        return this.departmentService.updateDepartment(departmentId, department)
                .doOnNext(departmentResponse -> log.info("{}", departmentResponse))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{departmentId}")
    public Mono<ResponseEntity<Void>> deleteDepartment(@PathVariable Long departmentId) {
        return this.departmentService.deleteDepartment(departmentId)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
