package dev.magadiflo.app.integration.repository;

import dev.magadiflo.app.config.TestDatabaseConfig;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ContextConfiguration;

@DataR2dbcTest
@ContextConfiguration(classes = TestDatabaseConfig.class)
class EmployeeRepositoryTest {

}