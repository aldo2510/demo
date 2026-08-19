# Java 25 External API Demo

Backend REST construido con Java 25, Spring Boot 4.1.0 y Maven.

## Endpoints

- `GET /api/v1/hello` — saludo y contador en memoria.
- `GET /api/v1/status` — estado de la aplicación y versión de Java.
- `GET /api/v1/requests` — historial reciente de requests almacenado en memoria.
- `GET /api/v1/github/repos/{owner}/{repo}` — consulta información pública de un repositorio usando la API de GitHub.

## Ejecutar

```bash
mvn spring-boot:run
```

## Tests y build

```bash
mvn clean verify
```

La CI de GitHub Actions ejecuta validaciones con Java 25, cache de Maven, tests, Checkstyle y empaqueta el JAR.
