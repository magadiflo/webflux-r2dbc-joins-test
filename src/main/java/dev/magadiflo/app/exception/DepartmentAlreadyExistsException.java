package dev.magadiflo.app.exception;

public class DepartmentAlreadyExistsException extends RuntimeException {
    public DepartmentAlreadyExistsException(String name) {
        super("El departamento con nombre %s ya existe".formatted(name));
    }
}
