# Java 25 External API Demo

Backend REST construido con Java 25, Spring Boot 4.1.0 y Maven.

## Endpoints

| Método | Endpoint | Propósito |
|---|---|---|
| GET | `/api/v1/hello` | Saludo y contador global de requests. |
| GET | `/api/v1/status` | Estado, versión de Java y contador. |
| GET | `/api/v1/memory` | Datos precargados en memoria + requests recientes. |
| GET | `/api/v1/requests` | Historial reciente de requests en memoria. |
| GET | `/api/v1/github/repos/{owner}/{repo}` | Consulta la API pública de GitHub. |
| GET | `/actuator/health` | Health check de Spring Boot Actuator. |

## Ejemplos

```bash
curl http://localhost:8080/api/v1/hello
curl http://localhost:8080/api/v1/status
curl http://localhost:8080/api/v1/memory
curl http://localhost:8080/api/v1/github/repos/aldo2510/demo
curl http://localhost:8080/actuator/health
```

## Arquitectura

- `api/`: controllers REST.
- `service/`: lógica de negocio y cliente de API externa.
- `record`: DTO inmutable para la respuesta de GitHub.
- `ConcurrentLinkedDeque` + `AtomicLong`: almacenamiento seguro en memoria.
- Actuator: endpoint de health check.
- Checkstyle: validación estática durante `mvn verify`.

## Ejecutar

```bash
mvn spring-boot:run
```

## Tests y build

```bash
mvn clean verify
```

## CI

GitHub Actions ejecuta en cada push a `main` y en Pull Requests:

1. Checkout.
2. Java 25 Temurin.
3. Cache de dependencias Maven.
4. `mvn clean verify` — compilación, tests, Checkstyle y empaquetado.
5. Publicación del JAR como artifact de CI.
