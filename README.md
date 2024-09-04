# [Joins with Spring Data R2DBC](https://neilwhite.ca/joins-with-spring-data-r2dbc/)

---

`Spring Data R2DBC` nos permite escribir código sin bloqueo para interactuar con las bases de datos. A diferencia de los
otros proyectos de `Spring Data`, `Spring Data R2DBC` no es un ORM y tiene algunas limitaciones. **Una de esas
limitaciones es la asignación de combinaciones para entidades.** Esto presenta un desafío para aquellos familiarizados
con `Spring Data JPA`.

> **¿Cómo se escribe código que no bloquee, al mismo tiempo que se unen y mapean entidades complejas?**

## Spring Data R2DBC

Al igual que otros proyectos de `Spring Data`, el objetivo de `Spring Data R2DBC` es facilitar el trabajo con bases de
datos. Para lograrlo de forma reactiva, tuvieron que eliminar muchas funciones. `Spring Data R2DBC` no es un marco ORM
y `no admite joins`.

Para una entidad simple, sin relaciones, `R2DBC` funciona muy bien. Cree una clase de entidad y un `repository` de
soporte.

Cuando se trata de entidades con relaciones, este patrón ya no funciona. Para superar esto, necesitamos crear nuestra
propia implementación de `repository` utilizando `DatabaseClient`.

## Las entidades

Utilizaremos dos entidades, un `Employee` básico y un `Department` que tiene dos relaciones diferentes con el
`Employee`.

Si traducimos estas entidades a tablas relacionadas en la base de datos, tendríamos el siguiente esquema.

![01.png](assets/01.png)

Para nuestro caso de negocio, diremos lo siguiente:

- Un departamento solo tiene un gerente (manager).
- Un empleado puede ser gerente de un único departamento.
- Un departamento tiene muchos empleados.
- Un empleado solo pertenece a un departamento.

Basándonos en el esquema definido, vamos a crear las instrucciones `DDL` para crear las tablas. Estas instrucciones las
crearemos en el archivo `src/main/resources/schema.sql`.

````sql
CREATE TABLE IF NOT EXISTS employees(
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    is_full_time BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS departments(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS department_managers(
    department_id BIGINT,
    employee_id BIGINT,
    CONSTRAINT pk_dm PRIMARY KEY(department_id, employee_id),
    CONSTRAINT fk_departments_dm FOREIGN KEY(department_id) REFERENCES departments(id),
    CONSTRAINT fk_employees_dm FOREIGN KEY(employee_id) REFERENCES employees(id),
    CONSTRAINT uk_department_id_dm UNIQUE(department_id),
    CONSTRAINT uk_employee_id_dm UNIQUE(employee_id)
);

CREATE TABLE IF NOT EXISTS department_employees(
    department_id BIGINT,
    employee_id BIGINT,
    CONSTRAINT pk_de PRIMARY KEY(department_id, employee_id),
    CONSTRAINT fk_departments_de FOREIGN KEY(department_id) REFERENCES departments(id),
    CONSTRAINT fk_employees_de FOREIGN KEY(employee_id) REFERENCES employees(id),
    CONSTRAINT uk_employee_id_de UNIQUE(employee_id)
);
````

Ahora, en el mismo directorio crearemos el archivo `src/main/resources/data.sql` con las instrucciones `DML` para
poblar las tablas.

Es importante notar que las dos primeras líneas, son instrucciones que permiten truncar las tablas principales
`departments` y `employees`, dado que, cada vez que levantemos la aplicación, vamos a volver a poblar las tablas
con los mismos registros. Usar `CASCADE` eliminará todos los datos de las tablas relacionadas.

````sql
TRUNCATE TABLE departments RESTART IDENTITY CASCADE;
TRUNCATE TABLE employees RESTART IDENTITY CASCADE;

INSERT INTO employees(first_name, last_name, position, is_full_time)
VALUES('Carlos', 'Gómez', 'Gerente', true),
('Ana', 'Martínez', 'Desarrollador', true),
('Luis', 'Fernández', 'Diseñador', false),
('María', 'Rodríguez', 'Analista', true),
('José', 'Pérez', 'Soporte', true),
('Laura', 'Sánchez', 'Desarrollador', true),
('Jorge', 'López', 'Analista', false),
('Sofía', 'Díaz', 'Gerente', true),
('Manuel', 'Torres', 'Soporte', true),
('Lucía', 'Morales', 'Diseñador', true),
('Miguel', 'Hernández', 'Desarrollador', true),
('Elena', 'Ruiz', 'Analista', false),
('Pablo', 'Jiménez', 'Desarrollador', true),
('Carmen', 'Navarro', 'Soporte', true),
('Raúl', 'Domínguez', 'Gerente', true),
('Beatriz', 'Vargas', 'Desarrollador', true),
('Francisco', 'Muñoz', 'Soporte', true),
('Marta', 'Ortega', 'Diseñador', false),
('Andrés', 'Castillo', 'Analista', true),
('Isabel', 'Ramos', 'Desarrollador', true);

INSERT INTO departments(name)
VALUES('Recursos Humanos'),
('Tecnología'),
('Finanzas'),
('Marketing'),
('Ventas');

INSERT INTO department_managers(department_id, employee_id)
VALUES(1, 1),  -- Recursos Humanos - Carlos Gómez
(2, 8),  -- Tecnología - Sofía Díaz
(3, 15), -- Finanzas - Raúl Domínguez
(4, 4),  -- Marketing - María Rodríguez
(5, 20); -- Ventas - Isabel Ramos

INSERT INTO department_employees(department_id, employee_id)
VALUES(1, 5),  -- Recursos Humanos - José Pérez
(1, 7),  -- Recursos Humanos - Jorge López
(2, 2),  -- Tecnología - Ana Martínez
(2, 6),  -- Tecnología - Laura Sánchez
(2, 11), -- Tecnología - Miguel Hernández
(2, 13), -- Tecnología - Pablo Jiménez
(2, 20), -- Tecnología - Isabel Ramos
(3, 10), -- Finanzas - Lucía Morales
(4, 18), -- Marketing - Marta Ortega
(4, 12), -- Marketing - Elena Ruiz
(5, 3),  -- Ventas - Luis Fernández
(5, 9),  -- Ventas - Manuel Torres
(5, 14), -- Ventas - Carmen Navarro
(5, 16), -- Ventas - Beatriz Vargas
(5, 17), -- Ventas - Francisco Muñoz
(5, 19); -- Ventas - Andrés Castillo
````

## Configura propiedades de la aplicación

En el `application.yml` agregaremos las siguientes configuraciones. Notar que la base de datos que estamos usando
se llama `db_webflux_r2dbc`. Esta base de datos la crearemos en el siguiente apartado.

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: webflux-r2dbc-joins
  r2dbc:
    url: r2dbc:postgresql://localhost:5433/db_webflux_r2dbc
    username: magadiflo
    password: magadiflo

logging:
  level:
    dev.magadiflo.app: DEBUG
    io.r2dbc.postgresql.QUERY: DEBUG
    io.r2dbc.postgresql.PARAM: DEBUG
````

## Base de datos Postgres con docker compose

Vamos a crear nuestra base de datos usando `compose` de docker.

````yml
services:
  postgres:
    image: postgres:15.2-alpine
    container_name: c-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_webflux_r2dbc
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - '5433:5432'
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
    name: postgres_data
````
