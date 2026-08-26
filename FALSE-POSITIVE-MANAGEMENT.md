# Laboratorio DevSecOps — Gestión de Falsos Positivos

**Duración máxima: 50 minutos**

Este laboratorio ya está **implementado y automatizado**. El alumno no construye el scanner desde cero: ejecuta el pipeline, interpreta findings, revisa la política, gestiona un false positive y corrige un true positive.

## Objetivo

Demostrar el flujo:

```text
SAST → Findings → Criticidad → Triage → Excepción FP → Quality Gate → Remediación
```

La presentación plantea reducir alert fatigue mediante tuning, análisis contextual y priorización de señales de alta confianza; también propone combinar severidad y confidence para decidir qué bloquea. fileciteturn13file0L11-L17 fileciteturn13file1L27-L30

## Resultado preparado

| Finding | Severidad | Resultado |
|---|---:|---|
| `insurance.command-injection` | **CRITICAL** | 🔴 Bloquea |
| `insurance.dynamic-uri` | **HIGH** | 🟡 False positive documentado |
| `insurance.console-log` | **MEDIUM** | 🟢 Report-only |

La política está en `security/policy.yml`; las reglas en `security/semgrep.yml`; la excepción en `security/false-positives.yml`; y el gate automatizado en `security/gate.py`.

---

# Paso 1 — Ejecutar el pipeline (7 min)

En GitHub:

```text
Actions → Security Quality Gate
```

El workflow ejecuta automáticamente:

```text
Checkout
  ↓
Semgrep
  ↓
semgrep.json
  ↓
security/gate.py
  ↓
Quality Gate
```

El primer resultado debe ser **FAIL** porque existe un CRITICAL real.

---

# Paso 2 — Analizar los findings (8 min)

Abrir:

```text
src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
```

### CRITICAL

```java
Runtime.getRuntime().exec(command);
```

Es un True Positive intencional. **Debe corregirse; no debe marcarse como FP.**

### HIGH

```java
URI.create("https://risk.example/api/" + internalRiskReference);
```

La regla detecta construcción dinámica, pero el escenario del laboratorio establece que la referencia procede de una integración interna confiable y no de entrada HTTP.

### MEDIUM

```java
System.out.println(message);
```

Se reporta, pero no bloquea.

---

# Paso 3 — Revisar la política (5 min)

Abrir `security/policy.yml`:

```yaml
blocking_severities:
  - CRITICAL
  - HIGH

non_blocking_severities:
  - MEDIUM
  - LOW
```

La pregunta es:

> ¿Por qué no usamos `continue-on-error: true`?

Porque eso ocultaría el resultado del análisis en lugar de tomar una decisión de riesgo.

---

# Paso 4 — Investigar el false positive (10 min)

Abrir `security/false-positives.yml`.

Revisar:

```yaml
rule_id: insurance.dynamic-uri
file: src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
reason: ...
owner: appsec-training
review_date: 2026-12-31
```

Responder:

1. ¿Qué regla generó el finding?
2. ¿Qué patrón busca?
3. ¿De dónde viene el dato?
4. ¿Existe input controlado por el usuario?
5. ¿Qué evidencia permite afirmar que es FP?
6. ¿Quién es el owner?
7. ¿Cuándo debe revisarse?

La presentación propone revisar el data flow antes de resolver un bloqueo y registrar la justificación del false positive. fileciteturn13file4L72-L75

---

# Paso 5 — Demostrar que la excepción importa (8 min)

Eliminar temporalmente la excepción `insurance.dynamic-uri` de `security/false-positives.yml`.

```bash
git add security/false-positives.yml
git commit -m "lab: remove false-positive exception"
git push
```

Resultado esperado:

```text
[BLOCK] HIGH insurance.dynamic-uri
QUALITY GATE: FAIL
```

Restaurar la excepción.

Ahora el HIGH deja de bloquear, pero el CRITICAL **continúa bloqueando**.

Este es el punto central del ejercicio: **gestionar un false positive no significa apagar el Quality Gate.**

---

# Paso 6 — Corregir el True Positive (7 min)

Modificar el método vulnerable de `SecurityLabExamples.java` para que no permita ejecutar comandos arbitrarios.

Por ejemplo, una implementación pedagógica puede utilizar una allowlist explícita:

```java
public static String executeApprovedOperation(String operation) {
    return switch (operation) {
        case "HEALTH" -> "OK";
        case "VERSION" -> "1.0";
        default -> throw new IllegalArgumentException("Operation not allowed");
    };
}
```

La idea es distinguir claramente:

```text
True Positive  → CORREGIR
False Positive → DOCUMENTAR + EXCEPCIÓN
```

---

# Paso 7 — Resultado final (5 min)

Ejecutar nuevamente el workflow.

Resultado esperado:

```text
HIGH    → documented FP → SUPPRESSED
MEDIUM  → REPORT ONLY
CRITICAL → corrected → NOT FOUND

QUALITY GATE: PASS
```

## Flujo final

```text
                 FINDING
                    |
        +-----------+-----------+
        |                       |
   TRUE POSITIVE          FALSE POSITIVE
        |                       |
      FIX                 EVIDENCE + OWNER
        |                       |
        |                  NARROW EXCEPTION
        |                       |
        +-----------+-----------+
                    |
                    v
             QUALITY GATE
                    |
                   PASS
```

---

# Preguntas de cierre

### ¿Por qué MEDIUM no bloquea?

Porque la política prioriza el camino crítico para HIGH/CRITICAL, manteniendo los demás findings visibles.

### ¿Por qué el HIGH necesita evidencia?

Porque “no quiero corregirlo” no significa “false positive”.

### ¿Qué debe tener una excepción?

```text
Rule ID
File/scope
Reason
Owner
Review date
```

### ¿Qué pasa con un CRITICAL real?

Se corrige. No se etiqueta como FP para obtener un pipeline verde.

La presentación insiste en reducir el ruido sin perder vulnerabilidades reales y en mantener trazabilidad y revisión de excepciones. fileciteturn13file5L87-L90 fileciteturn13file1L27-L30

---

# Archivos del laboratorio

```text
.github/workflows/security-quality-gate.yml
security/semgrep.yml
security/policy.yml
security/false-positives.yml
security/gate.py
src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
LAB-50-MIN.md
```

**Tiempo total: 50 minutos máximo.**
