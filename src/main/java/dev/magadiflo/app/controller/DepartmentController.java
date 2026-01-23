package dev.magadiflo.app.controller;

import dev.magadiflo.app.dto.DepartmentResponse;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<DepartmentResponse>>> findAllDepartments() {
        return Mono.just(ResponseEntity.ok(this.departmentService.getAllDepartments()));
    }

    @GetMapping(path = "/{departmentId}/stream-employees", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<EmployeeResponse>>> getEmployeesFromDepartment(@PathVariable Long departmentId,
                                                                                   @RequestParam(required = false) Boolean fullTime) {
        return Mono.just(ResponseEntity.ok(this.departmentService.getEmployeesFromDepartment(departmentId, fullTime)));
    }

    @GetMapping(path = "/{departmentId}")
    public Mono<ResponseEntity<DepartmentResponse>> findDepartment(@PathVariable Long departmentId) {
        return this.departmentService.showDepartment(departmentId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(path = "/{departmentId}/manager-employees")
    public Mono<ResponseEntity<DepartmentResponse>> findWithManagerAndEmployees(@PathVariable Long departmentId) {
        return this.departmentService.showDepartmentWithManagerAndEmployees(departmentId)
                .map(ResponseEntity::ok);
    }
    
}
