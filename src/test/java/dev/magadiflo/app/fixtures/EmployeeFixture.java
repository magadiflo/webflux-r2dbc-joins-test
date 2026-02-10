package dev.magadiflo.app.fixtures;

import dev.magadiflo.app.dto.EmployeeResponse;
import dev.magadiflo.app.entity.Employee;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EmployeeFixture {
    public static Employee createFullTimeEmployee(Long id, String firstName, String lastName) {
        return Employee.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .position("Developer")
                .fullTime(true)
                .build();
    }

    public static Employee createDefaultEmployee() {
        return EmployeeFixture.createFullTimeEmployee(1L, "Lesly", "Águila");
    }

    public static Employee createPartTimeEmployee() {
        return Employee.builder()
                .id(2L)
                .firstName("Jorge")
                .lastName("Gayoso")
                .position("Designer")
                .fullTime(false)
                .build();
    }

    public static Employee createEmployee(Long id, String firstName, String lastName,
                                          String position, boolean fullTime) {
        return Employee.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .position(position)
                .fullTime(fullTime)
                .build();
    }

    public static Employee createDeveloper(Long id, boolean fullTime) {
        return EmployeeFixture.createEmployee(id, "FirstName-dev-" + id,
                "LastName-dev-" + id, "Developer", fullTime);
    }

    public static Employee createDesigner(Long id, boolean fullTime) {
        return EmployeeFixture.createEmployee(id, "FirstName-designer-" + id,
                "LastName-designer-" + id, "Designer", fullTime);
    }

    public static EmployeeResponse toEmployeeResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getPosition(),
                employee.getFullTime());
    }

}
