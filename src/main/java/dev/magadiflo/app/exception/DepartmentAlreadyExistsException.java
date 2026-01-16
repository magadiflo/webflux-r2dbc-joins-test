package dev.magadiflo.app.exception;

public class DepartmentAlreadyExistsException extends RuntimeException {
    public DepartmentAlreadyExistsException(String name) {
        super("El nombre del departamento [%s] ya existe".formatted(name));
    }
}
