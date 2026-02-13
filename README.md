# 🔗 Joins con Spring Data R2DBC | Unit & Integration Test

- [Repositorio de referencia: reactive-spring-demo](https://github.com/neil-writes-code/reactive-spring-demo)

---

## 📌 Concepto clave

`Spring Data R2DBC` permite interactuar con bases de datos de manera reactiva y sin bloqueo.
A diferencia de `Spring Data JPA`, no es un ORM y por lo tanto no soporta joins automáticos ni relaciones complejas
entre entidades.

👉 Esto significa que si vienes del mundo JPA/Hibernate, **no encontrarás anotaciones como**
`@OneToMany`, `@ManyToOne` o `@JoinColumn` funcionando de forma automática.

### ⚠️ Limitación principal

- No hay soporte nativo para joins entre entidades.
- Cada entidad se mapea de forma independiente.
- Si necesitas combinar datos de varias tablas, debes hacerlo manualmente.

### 🛠️ Estrategia para superar la limitación

Para manejar relaciones y joins en R2DBC, se recomienda:

1. Usar `DatabaseClient` o `R2dbcEntityTemplate`: Permiten ejecutar queries SQL personalizados y mapear los resultados
   a objetos.
2. Crear `DTOs` específicos para resultados combinados: En lugar de intentar mapear directamente a entidades
   relacionadas, defines clases como `DepartmentResponse` que representen el resultado del join.
3. Separar responsabilidades:
    - `Entidades` → representan tablas individuales.
    - `DTOs` → representan vistas o combinaciones de datos.
    - `Repositorios personalizados` → encapsulan queries complejas.

### ✅ Buenas prácticas en proyectos reales

- Mantén las entidades simples (solo columnas directas).
- Usa `repositorios personalizados` para queries con joins.
- Documenta claramente qué queries son reactivas y cómo se manejan los resultados.
- Evita replicar el modelo relacional completo en el código; piensa en agregados y casos de uso.

## 🐳 Docker Compose: Bases de datos para desarrollo y test

Definir contenedores de PostgreSQL para:

- `Desarrollo` → base de datos principal (`db_webflux_r2dbc`).
- `Test` → base de datos aislada (`db_webflux_r2dbc_test`).

Esto asegura que las pruebas no afecten los datos de desarrollo.

### ⚙️ Configuración

````yml
services:
  s-postgres-webflux:
    image: postgres:17-alpine
    container_name: c-postgres-webflux
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_webflux_r2dbc
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - '5435:5432'
    volumes:
      - postgres-webflux-data:/var/lib/postgresql/data
    networks:
      - webflux-net

  s-postgres-webflux-test:
    image: postgres:17-alpine
    container_name: c-postgres-webflux-test
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_webflux_r2dbc_test
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - '5436:5432'
    networks:
      - webflux-net

volumes:
  postgres-webflux-data:
    name: postgres-webflux-data

networks:
  webflux-net:
    name: webflux-net
````

### 📝 Notas importantes

- 📂 `Volumen persistente`: solo se define para la BD de desarrollo, ya que en test normalmente se recrea la BD en cada
  ejecución.
- 🔒 `Usuarios y contraseñas`: mantenerlos en variables de entorno externas (`.env`) para mayor seguridad.
- 🧪 `Base de datos de test`: se recomienda inicializar con datos controlados (scripts o Testcontainers) para pruebas
  reproducibles.
- 🌐 `Red compartida (webflux-net)`: permite que los servicios se comuniquen entre sí.
