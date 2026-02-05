package dev.magadiflo.app.integration.dao;

import dev.magadiflo.app.dao.DepartmentDao;
import dev.magadiflo.app.entity.Department;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
class DepartmentDaoTest {

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private DatabaseClient databaseClient;

    private static String dataSql;

    @BeforeAll
    static void beforeAll() throws IOException {
        Resource dataResource = new ClassPathResource("sql-test/data.sql");
        Path dataPath = Paths.get(dataResource.getURI());
        byte[] dataRead = Files.readAllBytes(dataPath);
        dataSql = new String(dataRead);
    }

    @BeforeEach
    void setUp() {
        this.databaseClient
                .sql(dataSql)
                .fetch()
                .rowsUpdated()
                .block();
    }

    @Test
    void shouldFindAllDepartments() {
        Flux<Department> departmentFlux = this.departmentDao.findAll();

        StepVerifier.create(departmentFlux)
                .expectNextMatches(department -> department.getName().equals("Tecnología"))
                .expectNextMatches(department -> department.getName().equals("Ventas"))
                .expectNextMatches(department -> department.getName().equals("Legal"))
                .expectNextMatches(department -> department.getName().equals("Soporte"))
                .verifyComplete();
    }
}
