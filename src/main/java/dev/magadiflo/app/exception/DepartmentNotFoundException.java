package dev.magadiflo.app.exception;

public class DepartmentNotFoundException extends RuntimeException {
    public DepartmentNotFoundException(Long departmentId) {
        super("El departamento con id %d no fue encontrado".formatted(departmentId));
    }
}
