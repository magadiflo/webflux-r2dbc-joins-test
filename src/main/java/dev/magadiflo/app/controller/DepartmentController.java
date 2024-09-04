package dev.magadiflo.app.controller;

import dev.magadiflo.app.model.dto.CreateDepartmentRequest;
import dev.magadiflo.app.model.entity.Department;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Department>>> findAllDepartments() {
        return Mono.just(ResponseEntity.ok(this.departmentService.getAllDepartments()));
    }

    @GetMapping(path = "/{departmentId}")
    public Mono<ResponseEntity<Department>> findDepartment(@PathVariable Long departmentId) {
        return this.departmentService.showDepartmentWithManagerAndEmployees(departmentId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/{departmentId}/employees")
    public Mono<ResponseEntity<Flux<Employee>>> getEmployeesFromDepartment(@PathVariable Long departmentId,
                                                                           @RequestParam(name = "fullTime", required = false) Boolean isFullTime) {
        return Mono.just(ResponseEntity.ok(this.departmentService.getEmployeesFromDepartment(departmentId, isFullTime)));
    }

    @PostMapping
    public Mono<ResponseEntity<Department>> saveDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        return this.departmentService.createDepartment(request)
                .map(departmentDB -> new ResponseEntity<>(departmentDB, HttpStatus.CREATED));
    }

    @PutMapping(path = "/{departmentId}")
    public Mono<ResponseEntity<Department>> updateDepartment(@PathVariable Long departmentId, @RequestBody Department department) {
        return this.departmentService.updateDepartment(departmentId, department)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{departmentId}")
    public Mono<ResponseEntity<Void>> deleteDepartment(@PathVariable Long departmentId) {
        return this.departmentService.deleteDepartment(departmentId)
                .thenReturn(ResponseEntity.noContent().build());
    }

}
