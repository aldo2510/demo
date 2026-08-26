# 🧪 Laboratorio — Gestión de Falsos Positivos

**Duración máxima: 50 minutos**  
**Branch:** `exercise/false-positive-management`

> El laboratorio ya está **implementado y automatizado**. El alumno no debe construir el scanner desde cero. La actividad consiste en ejecutar el pipeline, interpretar los findings, entender la política, gestionar un falso positivo y corregir el riesgo real que mantiene bloqueado el Quality Gate.

---

## 🎯 Qué vamos a demostrar

```text
SAST
  ↓
Findings
  ↓
Clasificación por criticidad
  ↓
Excepción documentada de FP
  ↓
Quality Gate
  ↓
CRITICAL/HIGH → BLOCK
MEDIUM/LOW    → REPORT
```

El ejercicio materializa las ideas de la presentación: **alert fatigue, tuning, triage, análisis contextual, severidad, confidence y gestión trazable de excepciones**. fileciteturn13file0L11-L17 fileciteturn13file1L27-L30

---

# ⏱️ Agenda

| Tiempo | Actividad |
|---:|---|
| 0–5 | Contexto y objetivo |
| 5–12 | Ejecutar pipeline y observar findings |
| 12–20 | Analizar criticidad y Quality Gate |
| 20–30 | Investigar el false positive |
| 30–38 | Eliminar/restaurar excepción y observar comportamiento |
| 38–45 | Corregir el CRITICAL real |
| 45–50 | Re-run, resultado y conclusiones |

---

# 1. Contexto — 5 min

La aplicación es una Insurance Core API con Java 25, Spring Boot y Maven. El repositorio ya cuenta con CI para build, tests, Checkstyle y JaCoCo. fileciteturn15file0L2-L6

El laboratorio agrega:

```text
.github/workflows/security-quality-gate.yml
security/semgrep.yml
security/policy.yml
security/false-positives.yml
security/gate.py
src/main/java/.../securitylab/SecurityLabExamples.java
```

La política es:

| Severidad | Resultado |
|---|---|
| CRITICAL | 🔴 BLOCK |
| HIGH | 🔴 BLOCK, salvo FP demostrado |
| MEDIUM | 🟢 REPORT |
| LOW | 🟢 REPORT |

**Importante:** un finding no desaparece porque sea non-blocking; sigue siendo visible.

---

# 2. Ejecutar el pipeline — 7 min

En GitHub:

```text
Actions
  → Security Quality Gate
```

También puedes ejecutar localmente si tienes Semgrep instalado:

```bash
semgrep --config security/semgrep.yml --json --output semgrep.json
python security/gate.py semgrep.json
```

El primer resultado debe ser **FAIL**.

El alumno debe identificar tres findings:

```text
CRITICAL → command injection
HIGH     → dynamic URI
MEDIUM   → console logging
```

---

# 3. Entender por qué falla — 8 min

Abrir:

```text
src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
```

### 🔴 CRITICAL — real

```java
Runtime.getRuntime().exec(command);
```

Este finding es intencionalmente vulnerable.

**Pregunta:**

> ¿Debemos marcarlo como false positive?

**Respuesta esperada:** no. Existe un riesgo real y debe corregirse.

### 🟡 HIGH — falso positivo preparado

```java
URI.create("https://risk.example/api/" + internalRiskReference);
```

La regla detecta construcción dinámica de URI, pero en este escenario el valor procede de una integración interna confiable y no de entrada HTTP.

### 🟢 MEDIUM

```java
System.out.println(message);
```

Es visible, pero no bloquea porque la política separa findings report-only de findings bloqueantes.

---

# 4. Investigar el false positive — 10 min

Abrir:

```text
security/false-positives.yml
```

Encontrar:

```yaml
rule_id: insurance.dynamic-uri
file: src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
reason: ...
owner: appsec-training
review_date: 2026-12-31
```

El alumno debe responder:

1. ¿Qué regla generó la alerta?
2. ¿Qué está buscando?
3. ¿De dónde viene el dato?
4. ¿Existe realmente un input controlado por el usuario?
5. ¿Qué evidencia permite clasificarlo como FP?
6. ¿Quién es el owner?
7. ¿Cuándo debe revisarse?

La presentación propone precisamente revisar el data flow antes de clasificar un finding como false positive y registrar la justificación. fileciteturn13file4L72-L75

---

# 5. Experimento: quitar la excepción — 8 min

Eliminar temporalmente el bloque `insurance.dynamic-uri` de:

```text
security/false-positives.yml
```

Commit y push:

```bash
git add security/false-positives.yml
git commit -m "lab: remove false-positive exception"
git push
```

El pipeline debe mostrar:

```text
[BLOCK] HIGH insurance.dynamic-uri

QUALITY GATE: FAIL
```

### Pregunta

> ¿Qué cambió?

No cambió el código. Cambió la **decisión documentada de gestión del finding**.

Ahora restaurar la excepción.

---

# 6. Corregir el riesgo real — 7 min

El pipeline todavía falla por el CRITICAL.

El objetivo es que el alumno modifique:

```java
Runtime.getRuntime().exec(command);
```

por una implementación segura que **no permita ejecutar comandos arbitrarios**.

Una alternativa pedagógica sencilla es reemplazar el método por una operación explícitamente permitida, por ejemplo:

```java
public static String executeApprovedOperation(String operation) {
    return switch (operation) {
        case "HEALTH" -> "OK";
        case "VERSION" -> "1.0";
        default -> throw new IllegalArgumentException("Operation not allowed");
    };
}
```

El objetivo no es memorizar esta implementación, sino demostrar que un **True Positive se corrige**, mientras que un false positive se gestiona con evidencia.

---

# 7. Resultado final — 5 min

Ejecutar nuevamente el workflow.

Resultado esperado:

```text
HIGH    → documented FP → SUPPRESSED
MEDIUM  → REPORT ONLY
CRITICAL → corrected → NOT FOUND

QUALITY GATE: PASS
```

### El mensaje que debe quedar

```text
False Positive
      ↓
Evidence
      ↓
Narrow Exception
      ↓
Traceability

True Positive
      ↓
Remediation
      ↓
Quality Gate PASS
```

---

# 🧠 Cierre — 5 minutos

Preguntas rápidas:

### 1. ¿Por qué no desactivamos SAST?

Porque eliminaríamos también la capacidad de detectar vulnerabilidades reales.

### 2. ¿Por qué MEDIUM no bloquea?

Porque la política prioriza el camino crítico para HIGH/CRITICAL, manteniendo los demás findings visibles.

### 3. ¿Por qué el HIGH necesita evidencia?

Porque “no quiero corregirlo” no significa “false positive”.

### 4. ¿Qué debe tener una excepción?

```text
Rule ID
File/scope
Reason
Owner
Review date
```

### 5. ¿Qué pasa con un CRITICAL real?

**Se corrige. No se etiqueta como FP para conseguir un pipeline verde.**

La presentación insiste en que el objetivo del triage es reducir ruido sin perder las vulnerabilidades reales y mantener las excepciones bajo revisión. fileciteturn13file5L87-L90 fileciteturn13file1L27-L30

---

# 📁 Material de apoyo

- `security/semgrep.yml` → reglas del laboratorio.
- `security/policy.yml` → criticidad y política.
- `security/false-positives.yml` → excepción documentada.
- `security/gate.py` → Quality Gate automatizado.
- `.github/workflows/security-quality-gate.yml` → CI/CD.
- `SecurityLabExamples.java` → findings controlados.
- `FALSE-POSITIVE-MANAGEMENT.md` → material extendido para instructor.

## Resultado esperado en 50 minutos

El alumno no solo sabe **qué es un false positive**: ha visto un pipeline fallar, ha investigado el finding, ha quitado/restaurado una excepción, ha corregido un True Positive y ha conseguido finalmente un **Quality Gate PASS sin desactivar seguridad**.
