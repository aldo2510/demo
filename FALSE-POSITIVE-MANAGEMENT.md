# Laboratorio DevSecOps — Gestión de Falsos Positivos, Criticidad y Quality Gate

> **Objetivo del laboratorio:** convertir un pipeline de seguridad ruidoso en un gate accionable, usando clasificación por criticidad, configuración centralizada, excepciones auditables y separación entre findings que bloquean y findings que deben quedar para revisión.

## Duración

**Tiempo recomendado: 120–150 minutos.**

- 10 min — Preparación y lectura del escenario
- 15 min — Levantamiento del baseline
- 25 min — Triage y clasificación de findings
- 20 min — Diseño de política por criticidad
- 25 min — Implementación de configuración y Quality Gate
- 15 min — Gestión documentada de falsos positivos
- 10 min — Validación con PR y re-run
- 10–30 min — Desafío opcional / discusión empresarial

La versión completa está pensada para ocupar prácticamente una sesión larga de laboratorio, especialmente si se pide al alumno justificar cada decisión antes de modificar el repositorio.

---

## 1. Contexto

El repositorio es una **Insurance Core API** construida con Java 25, Spring Boot 4.1 y Maven. La aplicación expone operaciones de clientes, pólizas, siniestros y una integración externa de riesgo. El repositorio ya tiene un workflow de GitHub Actions que ejecuta compilación, pruebas, Checkstyle, JaCoCo y empaquetado; además existe un workflow de Dependency Review. El laboratorio parte de ese pipeline y lo evoluciona hacia una política de seguridad orientada al riesgo.

La presentación del curso plantea cuatro ideas que usaremos directamente:

- reducir el **ruido** para evitar alert fatigue;
- investigar el contexto antes de marcar un finding como false positive;
- preferir excepciones de **mínimo alcance**;
- permitir que el Quality Gate sea estricto donde el riesgo realmente importa.

En la presentación, el false positive se define como una alerta que no representa realmente una vulnerabilidad en el contexto actual; también se diferencia de un false negative, donde el riesgo existe pero la herramienta no lo detecta. Asimismo, se propone el uso de severidad + confidence para decidir qué bloquea y qué se deriva a revisión asíncrona.

---

## 2. Reto del laboratorio

El equipo de desarrollo se queja de que cualquier finding termina bloqueando el Pull Request.

La nueva política solicitada por el equipo de AppSec es:

| Criticidad | Comportamiento esperado |
|---|---|
| **CRITICAL** | Bloquea el PR |
| **HIGH** | Bloquea el PR |
| **MEDIUM** | No bloquea; queda visible para tratamiento |
| **LOW** | No bloquea; queda visible para seguimiento |
| **FALSE POSITIVE** | No bloquea, pero exige evidencia y trazabilidad |
| **RISK ACCEPTED** | No se trata como false positive; requiere proceso de riesgo |

### Regla de oro

> **No queremos que el pipeline pase porque dejamos de analizar. Queremos que pase porque hemos decidido qué debe bloquear y qué debe gestionarse fuera del camino crítico.**

---

## 3. Antes de tocar el código: baseline

### 3.1. Clonar el repositorio

```bash
git clone https://github.com/aldo2510/insurance-core-api.git
cd insurance-core-api
```

### 3.2. Cambiar a la rama del laboratorio

```bash
git checkout exercise/false-positive-management
```

### 3.3. Ejecutar el estado inicial

```bash
mvn -B -ntp clean verify
```

Documenta:

- duración del build;
- resultado de pruebas;
- resultado de Checkstyle;
- cobertura JaCoCo;
- artifacts generados;
- estado del workflow en GitHub Actions.

### Pregunta de análisis

> ¿Qué parte del pipeline actual es un verdadero Quality Gate y qué parte solamente ejecuta una herramienta?

**No modifiques nada todavía.**

---

## 4. Levantamiento de la política

Antes de configurar una herramienta, crea una matriz de decisión.

### Matriz inicial

| Severidad | ¿Bloquea? | ¿Requiere revisión humana? | ¿Puede ser FP? |
|---|---:|---:|---:|
| CRITICAL | Sí | Sí | Sí, con evidencia fuerte |
| HIGH | Sí | Sí | Sí, con evidencia fuerte |
| MEDIUM | No | Sí | Sí |
| LOW | No | Opcional | Sí |

Completa la matriz incluyendo:

- owner;
- SLA de tratamiento;
- fecha de revisión;
- tratamiento de legacy;
- comportamiento en Pull Request;
- comportamiento en branch principal.

### Debate

¿Por qué conviene bloquear solamente HIGH/CRITICAL en vez de hacer `continue-on-error: true` sobre todo el análisis?

La presentación advierte explícitamente que hacer que el pipeline ignore globalmente el resultado destruye el objetivo del Quality Gate.

---

## 5. Identificar el tipo de finding

Clasifica cada hallazgo encontrado en una de estas categorías:

```text
A. True Positive
B. False Positive
C. Risk Accepted
D. False Negative sospechado
E. Evidencia insuficiente
```

### Criterios

**True Positive**
- el riesgo existe;
- el flujo es explotable o el comportamiento inseguro es válido en el contexto;
- requiere remediación.

**False Positive**
- la herramienta detecta un patrón;
- el análisis del contexto demuestra que la condición de riesgo no se cumple;
- la excepción debe quedar documentada.

**Risk Accepted**
- el finding es válido;
- la organización decide no remediarlo inmediatamente;
- debe seguir un proceso de aceptación de riesgo.

**Evidencia insuficiente**
- todavía no puedes demostrar que el riesgo no aplica;
- el finding permanece abierto.

---

## 6. Triage técnico

Para cada finding, documenta como mínimo:

```text
Rule ID:
Severidad:
Archivo:
Línea:
Fuente del dato:
Sink:
Ruta del dato:
Controles existentes:
Contexto funcional:
Resultado del análisis:
Owner:
Fecha de revisión:
```

### Preguntas obligatorias

1. ¿Qué regla generó el finding?
2. ¿Qué patrón está buscando?
3. ¿Existe una fuente controlada por el usuario?
4. ¿Existe realmente el sink peligroso?
5. ¿Existe sanitización, validación o encoding?
6. ¿Hay una capa adicional de protección?
7. ¿La herramienta entiende correctamente el framework?
8. ¿El hallazgo puede reproducirse?
9. ¿Qué cambia si modificamos una sola condición del código?

---

# 7. Ejercicio principal: configurar un Quality Gate orientado a criticidad

## Objetivo

Construir un gate con esta lógica:

```text
                    Finding
                       |
             +---------+---------+
             |                   |
       HIGH / CRITICAL      MEDIUM / LOW
             |                   |
           BLOCK             NO BLOCK
             |                   |
             v                   v
       Quality Gate FAIL     Quality Gate PASS
```

### Requisito adicional

El gate **no debe** desactivar el análisis para MEDIUM/LOW.

Los findings siguen generándose; simplemente no bloquean el flujo crítico.

---

## 8. Configuración del repositorio

Inspecciona primero la estructura actual:

```bash
find .github -maxdepth 3 -type f | sort
```

Revisa especialmente:

```text
.github/workflows/build.yml
.github/workflows/dependency-review.yml
pom.xml
checkstyle.xml
```

El objetivo es identificar qué puede convertirse en configuración centralizada y qué debe mantenerse en el código.

### Tarea

Diseña una configuración equivalente a:

```text
.github/
├── workflows/
│   ├── build.yml
│   ├── dependency-review.yml
│   └── security-quality-gate.yml
└── security/
    └── policy.yml
```

La política puede contener, como mínimo:

```yaml
severity:
  blocking:
    - CRITICAL
    - HIGH
  non_blocking:
    - MEDIUM
    - LOW

false_positive:
  require_comment: true
  require_owner: true
  require_review_date: true
```

> **Nota:** el formato anterior es parte del ejercicio pedagógico. No se exige que una herramienta concreta consuma exactamente este YAML; el participante debe adaptar la política al mecanismo de análisis elegido.

---

# 9. Diseñar el pipeline

El flujo esperado es:

```text
Push / Pull Request
        |
        v
 Build + Tests
        |
        v
 Security Scan
        |
        v
 Normalize Findings
        |
        v
 Apply Severity Policy
        |
   +----+----+
   |         |
 HIGH/CRIT  MED/LOW
   |         |
 BLOCK    REPORT ONLY
   |         |
   +----+----+
        |
        v
  Quality Gate
```

### Reglas

- Nunca uses `continue-on-error: true` para apagar globalmente el gate.
- El análisis sigue ejecutándose.
- Solo la decisión de bloqueo depende de criticidad.
- La política debe ser visible y revisable en Git.

---

# 10. Falsos positivos: el caso que rompe el pipeline

Crea un escenario donde la herramienta marque un finding que después pueda demostrarse como falso positivo.

El caso debe utilizar el dominio de **Insurance Core API**, por ejemplo:

- validación de entradas de customer;
- filtros de policy;
- estado de claim;
- referencia externa de risk.

### El ejercicio no consiste en escribir una excepción inmediatamente.

Primero debes construir la evidencia.

### Evidencia mínima

```text
1. Finding original
2. Regla
3. Severidad
4. Código involucrado
5. Flujo del dato
6. Control compensatorio
7. Demostración de que la condición requerida por la regla no se cumple
8. Justificación de FP
```

---

# 11. Gestión del false positive

Cuando exista evidencia suficiente, registra:

```text
Estado: FALSE POSITIVE
Rule ID: <id>
Severidad: <severity>
Owner: <equipo/persona>
Fecha: <fecha>
Review date: <fecha>

Justificación:
<explicación técnica>

Evidencia:
- <evidencia 1>
- <evidencia 2>
- <evidencia 3>

Impacto del cambio:
<qué riesgo residual queda>
```

### Regla de mínimo alcance

Preferencia:

```text
finding específico
      > regla específica
      > archivo específico
      > proyecto completo
      > herramienta completa
```

No aceptamos:

```text
Desactivar CodeQL
Desactivar SAST
Ignorar carpeta completa
Ignorar todos los findings
```

---

# 12. Confidence Score

La presentación propone combinar severidad y confianza. Una política didáctica puede ser:

```text
BLOCK =
  (severity IN [HIGH, CRITICAL])
  AND
  (confidence >= 90)
```

Mientras que:

```text
NO BLOCK =
  (severity IN [LOW, MEDIUM])
  OR
  (confidence < 90)
```

### Pregunta clave

¿Qué ocurre con un **CRITICAL con confidence 70%**?

El alumno debe justificar si la organización prefiere:

- bloquear por severidad;
- derivar a triage humano;
- usar una combinación severidad + confidence.

**No existe una respuesta universal. Lo importante es justificar la política.**

---

# 13. Escenario avanzado: dos PRs

Para maximizar el ejercicio, trabaja con dos ramas o dos Pull Requests conceptuales.

## PR #1 — Ruido

Introduce findings LOW/MEDIUM y un supuesto false positive.

Resultado esperado:

```text
Scan: PASS
Gate: PASS
PR: NO BLOCK
Findings: visibles
```

## PR #2 — Riesgo real

Introduce un finding HIGH o CRITICAL controlado para que el pipeline tenga que bloquear.

Resultado esperado:

```text
Scan: FINDING
Gate: FAIL
PR: BLOCKED
```

### Objetivo pedagógico

Demostrar que:

> **Reducir falsos positivos no significa reducir sensibilidad frente a los riesgos críticos.**

---

# 14. Experimento de tuning

Realiza tres ejecuciones modificando únicamente la política.

### Experimento A — Todo bloquea

```text
LOW      -> BLOCK
MEDIUM   -> BLOCK
HIGH     -> BLOCK
CRITICAL -> BLOCK
```

Registra el impacto en feedback y ruido.

### Experimento B — Solo HIGH/CRITICAL

```text
LOW      -> PASS
MEDIUM   -> PASS
HIGH     -> BLOCK
CRITICAL -> BLOCK
```

Registra qué findings siguen visibles.

### Experimento C — Severidad + confianza

```text
HIGH/CRITICAL + confidence >= 90 -> BLOCK
HIGH/CRITICAL + confidence < 90  -> TRIAGE
LOW/MEDIUM                       -> PASS
```

Compara los tres modelos.

---

# 15. Configuración de ramas y Pull Requests

Como parte del ejercicio, revisa la configuración del repositorio y decide qué controles deberían existir en `main`.

Propuesta:

```text
main
 ├── Pull Request obligatorio
 ├── Require status checks
 ├── Security Quality Gate requerido
 ├── Review requerido
 └── No merge si HIGH/CRITICAL bloquea
```

### Preguntas

- ¿Qué status check debe ser obligatorio?
- ¿Qué ocurre si el escaneo no está disponible?
- ¿Qué ocurre si el pipeline falla por infraestructura y no por seguridad?
- ¿Quién puede modificar la política?
- ¿Quién puede aprobar excepciones?

---

# 16. Audit Trail

Toda excepción debe dejar trazabilidad.

Crea una tabla o registro con:

| Finding | Severity | Decision | Owner | Date | Review Date |
|---|---|---|---|---|---|
| ... | ... | FALSE POSITIVE | ... | ... | ... |

### Métrica final

Calcula:

```text
False Positive Rate =
False Positives / Total Findings * 100
```

Y además:

```text
% de findings que bloquean
% de findings no bloqueantes
% de excepciones con owner
% de excepciones vencidas
```

---

# 17. Desafío adicional — SCA

La presentación dedica una sección específica a falsos positivos en SCA y **Reachability Analysis**.

Plantea este escenario:

> Una dependencia tiene una vulnerabilidad conocida, pero el código de la aplicación no alcanza la función vulnerable.

Investiga:

```text
¿La dependencia está realmente en ejecución?
¿Qué módulo importa la librería?
¿Qué función vulnerable se supone que es alcanzable?
¿Hay un camino de ejecución desde la aplicación?
```

### Resultado esperado

No descartes una vulnerabilidad únicamente porque “no la usamos”.

Demuestra técnicamente la **reachability** o la ausencia de ella.

---

# 18. Desafío adicional — Secret Scanning

Crea un dato sintético de alta entropía que parezca un secreto, pero que no sea un token real.

Ejemplo:

```text
INTERNAL_REFERENCE=7f8f5e8a1c0d4f91b6b9e3a5c7d2e1f0
```

Investiga:

- por qué podría ser detectado;
- qué patrón utiliza el detector;
- cómo diferenciar un ID de un secreto real;
- qué controles adicionales evitarían falsos positivos.

**Nunca uses credenciales reales para este laboratorio.**

---

# 19. Desafío final — Gobierno

Diseña una política para una organización con cientos de desarrolladores.

Debe responder:

1. ¿Quién puede marcar un false positive?
2. ¿Quién aprueba una excepción HIGH/CRITICAL?
3. ¿Qué evidencia es obligatoria?
4. ¿Cuándo expira una excepción?
5. ¿Quién la revisa?
6. ¿Cómo se mide el ruido?
7. ¿Cómo se evita que una excepción se convierta en una vulnerabilidad silenciosa?
8. ¿Qué ocurre cuando cambia la arquitectura?

### Modelo sugerido

```text
Developer
   |
   v
Security Finding
   |
   v
Triage
   |
 +------+----------------+
 |      |                |
 v      v                v
TP     FP          Risk Accepted
 |      |                |
Fix   Evidence           Risk Process
 |      |                |
 +------+----------------+
        |
        v
 Quality Gate
        |
        v
      Merge
```

---

# 20. Criterios de aceptación

El laboratorio está completo cuando puedas demostrar todo lo siguiente:

- [ ] El pipeline analiza seguridad sin ser desactivado.
- [ ] HIGH y CRITICAL bloquean el PR.
- [ ] LOW y MEDIUM siguen siendo visibles pero no bloquean.
- [ ] Existe una política de severidad versionada.
- [ ] Existe una prueba de un finding real que bloquea.
- [ ] Existe una prueba de un false positive documentado.
- [ ] La excepción tiene owner y fecha de revisión.
- [ ] No se utiliza `continue-on-error` para ignorar seguridad.
- [ ] La excepción es de mínimo alcance.
- [ ] Se conserva audit trail.
- [ ] Se calculan métricas de ruido y excepciones.
- [ ] El alumno puede explicar la diferencia entre TP, FP, FN y TN.

---

# 21. Preguntas para discusión final

### Pregunta 1

¿Es correcto dejar pasar todos los MEDIUM?

### Pregunta 2

¿Es correcto bloquear todos los HIGH sin revisar contexto?

### Pregunta 3

¿Un false positive deja de ser importante porque no bloquea?

### Pregunta 4

¿Qué pasa si una regla se convierte en un generador permanente de ruido?

### Pregunta 5

¿Conviene resolver el problema en la herramienta, en el código, en el pipeline o en la política?

### Pregunta 6

¿Qué métrica demuestra que realmente reducimos ruido y no solamente ocultamos findings?

---

# 22. Entregables del alumno

Al finalizar, entrega:

1. La política de severidad implementada.
2. El pipeline actualizado.
3. Un Pull Request que demuestre el bloqueo por HIGH/CRITICAL.
4. Un Pull Request o commit que demuestre el tratamiento de un false positive.
5. La evidencia del triage.
6. La matriz de clasificación.
7. Las métricas finales.
8. Una explicación de 5 minutos sobre por qué el nuevo gate es mejor que bloquear todo.

---

# 23. Resultado esperado

El alumno debe terminar con un concepto central:

> **La madurez DevSecOps no consiste en tener más alertas. Consiste en tener alertas más confiables, una política clara para bloquear y una gestión trazable para el resto.**

Este laboratorio debe reforzar exactamente el flujo de la presentación: triage, tuning, análisis contextual, severidad, confidence, gestión del false positive, audit trail y mejora continua.
