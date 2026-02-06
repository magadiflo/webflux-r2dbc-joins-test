package dev.magadiflo.app.controller;

import dev.magadiflo.app.dto.EmployeeRequest;
import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.service.EmployeeService;
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
@RequestMapping(path = "/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<EmployeeResponse>>> findAllEmployees(@RequestParam(required = false) String position,
                                                                         @RequestParam(required = false) Boolean fullTime) {
        Flux<EmployeeResponse> employeeResponseFlux = this.employeeService.getAllEmployees(position, fullTime)
                .doOnNext(employeeResponse -> log.info("{}", employeeResponse));
        return Mono.just(ResponseEntity.ok(employeeResponseFlux));
    }

    @GetMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<EmployeeResponse>> findEmployee(@PathVariable Long employeeId) {
        return this.employeeService.showEmployee(employeeId)
                .doOnNext(employeeResponse -> log.info("{}", employeeResponse))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<EmployeeResponse>> saveEmployee(@Valid @RequestBody Mono<EmployeeRequest> requestMono) {
        return requestMono
                .flatMap(this.employeeService::createEmployee)
                .doOnNext(employeeResponse -> log.info("{}", employeeResponse))
                .map(employeeResponse -> ResponseEntity.status(HttpStatus.CREATED).body(employeeResponse));
    }

    @PutMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<EmployeeResponse>> updateEmployee(@PathVariable Long employeeId,
                                                                 @Valid @RequestBody Mono<EmployeeRequest> requestMono) {
        return requestMono
                .flatMap(employeeRequest -> this.employeeService.updateEmployee(employeeId, employeeRequest))
                .doOnNext(employeeResponse -> log.info("{}", employeeResponse))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<Void>> deleteEmployee(@PathVariable Long employeeId) {
        return this.employeeService.deleteEmployee(employeeId)
                .thenReturn(ResponseEntity.noContent().build());
    }

}

