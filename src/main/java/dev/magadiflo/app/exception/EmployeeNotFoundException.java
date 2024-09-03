package dev.magadiflo.app.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long employeeId) {
        super("El empleado con id %d no fue encontrado".formatted(employeeId));
    }
}
