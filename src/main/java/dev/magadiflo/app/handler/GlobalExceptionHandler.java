package dev.magadiflo.app.handler;

import dev.magadiflo.app.exception.DepartmentAlreadyExistsException;
import dev.magadiflo.app.exception.DepartmentNotFoundException;
import dev.magadiflo.app.exception.EmployeeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            EmployeeNotFoundException.class,
            DepartmentNotFoundException.class
    })
    public Mono<ResponseEntity<ErrorResponse>> handleNotFoundException(Exception exception) {
        log.debug("handleNotFoundException:: {}", exception.getMessage());
        ErrorResponse response = new ErrorResponse(Map.of("message", exception.getMessage()));
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler(DepartmentAlreadyExistsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAlreadyExistsException(Exception exception) {
        log.debug("handleAlreadyExistsException:: {}", exception.getMessage());
        ErrorResponse response = new ErrorResponse(Map.of("message", exception.getMessage()));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleException(WebExchangeBindException exception) {
        List<String> errors = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        ErrorResponse response = new ErrorResponse(Map.of("errors", errors));
        return ResponseEntity.badRequest().body(response);
    }

}
