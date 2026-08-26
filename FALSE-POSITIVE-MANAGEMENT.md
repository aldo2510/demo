# Laboratorio DevSecOps — Gestión de Falsos Positivos, Criticidad y Quality Gate

## Duración

**Tiempo recomendado: 150–180 minutos.**

El laboratorio está diseñado para una sesión larga y puede extenderse hasta **3 horas** si se ejecutan los desafíos opcionales, se exige evidencia de cada decisión y se comparan las tres políticas de Quality Gate.

## Objetivo

Convertir un pipeline de seguridad ruidoso en un gate accionable usando:

- clasificación por criticidad;
- configuración centralizada;
- triage técnico;
- gestión documentada de falsos positivos;
- excepciones de mínimo alcance;
- audit trail;
- métricas;
- separación entre findings bloqueantes y no bloqueantes.

La presentación plantea precisamente reducir alert fatigue, distinguir TP/FP/FN/TN, hacer tuning de reglas, usar análisis contextual y gestionar excepciones con trazabilidad. fileciteturn1file0L24-L31 fileciteturn1file0L45-L55

---

# 1. Escenario

La aplicación es una **Insurance Core API** con Java 25, Spring Boot 4.1 y Maven. El repositorio ya tiene GitHub Actions para build, tests, Checkstyle, JaCoCo y empaquetado, además de Dependency Review. fileciteturn5file0L2-L6 fileciteturn4file0L2-L5

El equipo de desarrollo se queja de que cualquier alerta termina bloqueando el Pull Request.

La nueva política de AppSec será:

| Criticidad | Comportamiento |
|---|---|
| **CRITICAL** | Bloquea |
| **HIGH** | Bloquea |
| **MEDIUM** | Visible, no bloquea |
| **LOW** | Visible, no bloquea |
| **FALSE POSITIVE** | No bloquea, pero exige evidencia y trazabilidad |
| **RISK ACCEPTED** | No es FP; sigue proceso de riesgo |

> **Regla de oro:** no queremos que el pipeline pase porque dejamos de analizar; queremos que pase porque hemos decidido qué debe bloquear.

---

# 2. Baseline — 15 minutos

```bash
git clone https://github.com/aldo2510/insurance-core-api.git
cd insurance-core-api
git checkout exercise/false-positive-management
mvn -B -ntp clean verify
```

Documenta:

- duración;
- tests;
- Checkstyle;
- JaCoCo;
- artifacts;
- estado del workflow.

**Pregunta:** ¿qué parte del pipeline actual es un Quality Gate real y qué parte solamente ejecuta una herramienta?

No modifiques nada todavía.

---

# 3. Matriz de decisión — 20 minutos

Construye una matriz con:

| Severity | Block | Human Review | Can be FP | Owner | SLA |
|---|---:|---:|---:|---|---|
| CRITICAL | Sí | Sí | Sí* | | |
| HIGH | Sí | Sí | Sí* | | |
| MEDIUM | No | Sí | Sí | | |
| LOW | No | Opcional | Sí | | |

`*` solo con evidencia fuerte.

Discute por qué `continue-on-error: true` no es una solución: la presentación señala que apagar el gate completo destruye el objetivo del Quality Gate. fileciteturn1file0L98-L103

---

# 4. Triage técnico — 25 minutos

Para cada finding registra:

```text
Rule ID:
Severidad:
Archivo:
Línea:
Fuente:
Sink:
Ruta del dato:
Validaciones:
Controles compensatorios:
Contexto funcional:
Decisión:
Owner:
Review date:
```

Clasifica:

```text
A. True Positive
B. False Positive
C. Risk Accepted
D. False Negative sospechado
E. Evidencia insuficiente
```

Preguntas obligatorias:

1. ¿Qué regla lo generó?
2. ¿Qué patrón busca?
3. ¿Existe una fuente controlada por el usuario?
4. ¿Existe realmente el sink?
5. ¿Hay sanitización, validación o encoding?
6. ¿Existe otra capa de protección?
7. ¿Puede reproducirse?
8. ¿Qué evidencia demostraría que no aplica?

---

# 5. Ejercicio principal: Quality Gate por criticidad — 30 minutos

Construye:

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

**Importante:** MEDIUM/LOW continúan siendo analizados. Solo dejan de bloquear el camino crítico.

Diseña una configuración versionada como:

```text
.github/
├── workflows/
│   ├── build.yml
│   ├── dependency-review.yml
│   └── security-quality-gate.yml
└── security/
    └── policy.yml
```

Ejemplo pedagógico:

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

> Este YAML es una política del ejercicio; el alumno debe adaptarla al mecanismo de seguridad utilizado.

El pipeline esperado:

```text
PR
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
 +---- HIGH/CRITICAL ---> BLOCK
 |
 +---- MEDIUM/LOW ------> REPORT ONLY
 |
v
Quality Gate
```

---

# 6. Caso de falso positivo — 20 minutos

Crea un caso en el dominio Insurance Core API que pueda ser reportado por una herramienta y luego demostrado como FP. Puedes utilizar:

- validación de Customer;
- Policy;
- Claim;
- integración Risk.

**No escribas la excepción primero.** Primero demuestra técnicamente que el riesgo no aplica.

La evidencia mínima es:

```text
1. Finding original
2. Rule ID
3. Severity
4. Código
5. Fuente del dato
6. Sink
7. Flujo
8. Control compensatorio
9. Evidencia de por qué la condición de riesgo no se cumple
10. Justificación de FP
```

La presentación propone precisamente auditar el flujo de datos antes de resolver un bloqueo injusto. fileciteturn1file0L95-L103

---

# 7. Gestión de la excepción — 15 minutos

Registra:

```text
Estado: FALSE POSITIVE
Rule ID: <id>
Severidad: <severity>
Owner: <equipo>
Fecha: <fecha>
Review date: <fecha>

Justificación:
<explicación técnica>

Evidencia:
- <evidencia 1>
- <evidencia 2>
- <evidencia 3>
```

Aplica **least privilege para excepciones**:

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
Ignorar una carpeta completa
Ignorar todos los findings
```

La presentación exige trazabilidad, justificación, ownership y revisión periódica de excepciones. fileciteturn1file0L111-L113 fileciteturn1file0L124-L127

---

# 8. Confidence Score — 15 minutos

La presentación plantea combinar severidad y confianza. Prueba:

```text
BLOCK =
  severity IN [HIGH, CRITICAL]
  AND confidence >= 90
```

Analiza especialmente:

```text
CRITICAL + confidence 70%
```

Decide si debe:

- bloquear por severidad;
- pasar a triage humano;
- usar política híbrida.

La presentación usa como ejemplo un gate que bloquea cuando la severidad es alta y la confianza supera 90%, mientras que los casos de menor confianza pasan a revisión humana. fileciteturn1file0L78-L82

---

# 9. Dos Pull Requests — 15 minutos

## PR #1 — Ruido

Introduce LOW/MEDIUM + supuesto FP.

Esperado:

```text
Scan: PASS
Gate: PASS
PR: NO BLOCK
Findings: visibles
```

## PR #2 — Riesgo real

Introduce HIGH/CRITICAL controlado.

Esperado:

```text
Scan: FINDING
Gate: FAIL
PR: BLOCKED
```

Objetivo: demostrar que reducir falsos positivos **no** significa reducir sensibilidad ante riesgos críticos.

---

# 10. Experimento de tuning — 20 minutos

Ejecuta tres políticas y compara resultados.

### A — Todo bloquea

```text
LOW      -> BLOCK
MEDIUM   -> BLOCK
HIGH     -> BLOCK
CRITICAL -> BLOCK
```

### B — Solo HIGH/CRITICAL

```text
LOW      -> PASS
MEDIUM   -> PASS
HIGH     -> BLOCK
CRITICAL -> BLOCK
```

### C — Severidad + confianza

```text
HIGH/CRITICAL + confidence >= 90 -> BLOCK
HIGH/CRITICAL + confidence < 90  -> TRIAGE
LOW/MEDIUM                       -> PASS
```

Mide ruido, feedback y capacidad de detectar riesgos.

---

# 11. Branch Protection y Pull Request — 10 minutos

Decide qué debe proteger `main`:

```text
main
 ├── Pull Request obligatorio
 ├── Required status checks
 ├── Security Quality Gate requerido
 ├── Review requerido
 └── No merge si HIGH/CRITICAL bloquea
```

Discute:

- ¿Qué pasa si el escáner está caído?
- ¿Qué pasa si falla infraestructura?
- ¿Quién puede modificar la política?
- ¿Quién aprueba excepciones?

---

# 12. Audit Trail y métricas — 15 minutos

Registra:

| Finding | Severity | Decision | Owner | Date | Review Date |
|---|---|---|---|---|---|
| ... | ... | FALSE POSITIVE | ... | ... | ... |

Calcula:

```text
False Positive Rate = False Positives / Total Findings * 100
% findings bloqueantes
% findings no bloqueantes
% excepciones con owner
% excepciones vencidas
```

La presentación destaca que la gestión de falsos positivos es continua y que AppSec debe revisar periódicamente las excepciones. fileciteturn1file0L116-L127

---

# 13. Desafío SCA: Reachability — 15 minutos opcionales

Plantea una dependencia vulnerable donde la aplicación no alcance la función vulnerable.

Investiga:

```text
¿La dependencia está realmente en ejecución?
¿Qué módulo importa la librería?
¿Qué función vulnerable se supone que es alcanzable?
¿Existe un camino de ejecución?
```

No descartes una vulnerabilidad solo porque “no la usamos”: demuestra reachability o su ausencia. La presentación usa Reachability Analysis como técnica para reducir falsos positivos en SCA. fileciteturn1file0L37-L41

---

# 14. Desafío Secret Scanning — 10 minutos opcionales

Usa únicamente un valor sintético:

```text
INTERNAL_REFERENCE=7f8f5e8a1c0d4f91b6b9e3a5c7d2e1f0
```

Investiga por qué podría detectarse y cómo diferenciar un ID de un secreto real.

**Nunca uses credenciales reales.**

La presentación explica que los detectores pueden confundir identificadores largos con tokens y que la validación activa reduce este ruido. fileciteturn1file0L48-L52

---

# 15. Desafío de gobierno — 15 minutos opcionales

Diseña una política para cientos de desarrolladores:

1. ¿Quién puede marcar un FP?
2. ¿Quién aprueba una excepción HIGH/CRITICAL?
3. ¿Qué evidencia es obligatoria?
4. ¿Cuándo expira?
5. ¿Quién revisa?
6. ¿Cómo se mide el ruido?
7. ¿Cómo se evita ocultar vulnerabilidades?
8. ¿Qué pasa cuando cambia la arquitectura?

Modelo:

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

# 16. Criterios de aceptación

- [ ] El análisis de seguridad no fue desactivado.
- [ ] HIGH y CRITICAL bloquean.
- [ ] LOW y MEDIUM siguen visibles pero no bloquean.
- [ ] Existe política versionada.
- [ ] Existe un finding real que bloquea.
- [ ] Existe un FP documentado.
- [ ] La excepción tiene owner y review date.
- [ ] No se usa `continue-on-error` para ignorar seguridad.
- [ ] La excepción tiene mínimo alcance.
- [ ] Existe audit trail.
- [ ] Se calculan métricas.
- [ ] El alumno puede explicar TP, FP, FN y TN.

# 17. Entregables

1. Política de severidad.
2. Pipeline actualizado.
3. PR que demuestre bloqueo HIGH/CRITICAL.
4. PR/commit que demuestre tratamiento de FP.
5. Evidencia de triage.
6. Matriz de clasificación.
7. Métricas.
8. Presentación final de 5 minutos defendiendo el nuevo modelo.

# Resultado esperado

> **La madurez DevSecOps no consiste en tener más alertas. Consiste en tener alertas más confiables, una política clara para bloquear y una gestión trazable para el resto.**

El laboratorio sigue el hilo de la presentación: triage, tuning, análisis contextual, severidad, confidence, gestión del false positive, audit trail y mejora continua. fileciteturn1file0L45-L55 fileciteturn1file0L78-L82
