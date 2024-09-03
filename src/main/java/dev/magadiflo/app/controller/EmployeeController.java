package dev.magadiflo.app.controller;

import dev.magadiflo.app.model.dto.CreateEmployeeRequest;
import dev.magadiflo.app.model.entity.Employee;
import dev.magadiflo.app.service.EmployeeService;
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
@RequestMapping(path = "/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Mono<ResponseEntity<Flux<Employee>>> findAllEmployees(@RequestParam(required = false) String position,
                                                                 @RequestParam(name = "fullTime", required = false) Boolean isFullTime) {
        return Mono.just(ResponseEntity.ok(this.employeeService.getAllEmployees(position, isFullTime)));
    }

    @GetMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<Employee>> findEmployee(@PathVariable Long employeeId) {
        return this.employeeService.showEmployee(employeeId)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<Employee>> saveEmployee(@Valid @RequestBody CreateEmployeeRequest employeeRequest) {
        return this.employeeService.createEmployee(employeeRequest)
                .map(employeeDB -> new ResponseEntity<>(employeeDB, HttpStatus.CREATED));
    }

    @PutMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<Employee>> updateEmployee(@PathVariable Long employeeId, @RequestBody Employee employee) {
        return this.employeeService.updateEmployee(employeeId, employee)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<Void>> deleteEmployee(@PathVariable Long employeeId) {
        return this.employeeService.deleteEmployee(employeeId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
