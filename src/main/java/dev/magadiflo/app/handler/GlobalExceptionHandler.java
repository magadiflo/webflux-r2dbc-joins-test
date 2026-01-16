package dev.magadiflo.app.handler;

import dev.magadiflo.app.exception.DepartmentAlreadyExistsException;
import dev.magadiflo.app.exception.DepartmentNotFoundException;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleEmployeeNotFoundException(Exception ex) {
        log.debug("handleEmployeeNotFoundException: {}", ex.getMessage());
        var response = this.build(HttpStatus.NOT_FOUND, ex, problemDetail ->
                problemDetail.setTitle("Empleado no encontrado"));
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler(DepartmentNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDepartmentNotFoundException(Exception ex) {
        log.debug("handleDepartmentNotFoundException: {}", ex.getMessage());
        var response = this.build(HttpStatus.NOT_FOUND, ex, problemDetail ->
                problemDetail.setTitle("Departamento no encontrado"));
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler(DepartmentAlreadyExistsException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDepartmentAlreadyExistsException(Exception ex) {
        log.debug("handleDepartmentAlreadyExistsException: {}", ex.getMessage());
        var response = this.build(HttpStatus.BAD_REQUEST, ex, problemDetail ->
                problemDetail.setTitle("Departamento ya se encuentra registrado"));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleWebExchangeBindException(WebExchangeBindException ex) {
        log.debug("WebExchangeBindException: {}", ex.getMessage());
        Map<String, List<String>> errorsByField = ex.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        var response = this.build(HttpStatus.BAD_REQUEST, ex, problemDetail -> {
            problemDetail.setTitle("Error de validación de campos");
            problemDetail.setProperty("errors", errorsByField);
        });
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }


    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleException(Exception ex) {
        log.debug("handleException: {}", ex.getMessage(), ex);
        var response = this.build(HttpStatus.INTERNAL_SERVER_ERROR, ex, problemDetail ->
                problemDetail.setTitle("Ocurrió un error en el servidor. Por favor, contacta al administrador."));
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }


    private ProblemDetail build(HttpStatus status, Exception ex, Consumer<ProblemDetail> detailConsumer) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        detailConsumer.accept(problemDetail);
        return problemDetail;
    }
}
