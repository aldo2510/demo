# LAB — Gestión de falsos positivos y Quality Gate

> **Duración máxima: 50 minutos**  
> **Modalidad:** laboratorio guiado sobre GitHub Actions  
> **Nivel:** DevSecOps / AppSec intermedio

## 🎯 Objetivo

En este laboratorio vas a trabajar con un pipeline de seguridad **ya implementado** y aprenderás a tomar una decisión de seguridad basada en **criticidad y contexto**, en lugar de bloquear indiscriminadamente por cualquier alerta.

Al finalizar podrás demostrar que:

- **CRITICAL** y **HIGH** bloquean el Quality Gate.
- **MEDIUM** y **LOW** se reportan, pero no bloquean.
- Un **false positive** no se elimina simplemente: se documenta y se controla mediante una excepción de alcance limitado.
- Una excepción para un HIGH **no debe hacer que un CRITICAL deje de bloquear**.
- El pipeline sigue analizando; únicamente cambia la decisión del Quality Gate.

---

# 🧩 Escenario

El equipo de desarrollo de **Fictitious Insurance** está recibiendo demasiadas alertas de seguridad.

El equipo AppSec propone la siguiente política:

| Criticidad | Acción |
|---|---|
| 🔴 **CRITICAL** | **BLOCK** |
| 🟠 **HIGH** | **BLOCK** |
| 🟡 **MEDIUM** | Reportar, no bloquear |
| 🟢 **LOW** | Reportar, no bloquear |
| ⚪ **False Positive** | Documentar + excepción controlada |

El repositorio ya contiene un escaneo SAST con **Semgrep**, una política de severidad, un registro de excepciones y un script que convierte los findings en una decisión de Quality Gate.

Tu trabajo no es construir todo desde cero. **Tu trabajo es entender, demostrar y ajustar el comportamiento.**

---

# ⏱️ Agenda — 50 minutos

| Tiempo | Actividad |
|---:|---|
| 0–5 min | 1. Entender el escenario |
| 5–12 min | 2. Ejecutar y observar el Quality Gate |
| 12–20 min | 3. Analizar los findings |
| 20–30 min | 4. Gestionar el false positive |
| 30–38 min | 5. Demostrar que la excepción no elimina el riesgo crítico |
| 38–45 min | 6. Corregir el CRITICAL |
| 45–50 min | 7. Re-ejecutar y cerrar |

---

# 1. Entender el escenario — 5 min

Trabaja sobre la rama:

```bash
git clone https://github.com/aldo2510/insurance-core-api.git
cd insurance-core-api
git checkout exercise/false-positive-management
```

Revisa rápidamente estos archivos:

```text
security/
├── semgrep.yml
├── policy.yml
├── false-positives.yml
└── gate.py

.github/workflows/
└── security-quality-gate.yml

src/main/java/com/fictitious/insurance/securitylab/
└── SecurityLabExamples.java
```

### Pregunta rápida

Antes de ejecutar nada, identifica:

- ¿Dónde están definidas las reglas?
- ¿Dónde está definida la criticidad?
- ¿Dónde se registran las excepciones?
- ¿Dónde se decide si el pipeline falla?

**No cambies nada todavía.**

---

# 2. Ejecutar y observar el Quality Gate — 7 min

El workflow ya está configurado en:

```text
.github/workflows/security-quality-gate.yml
```

Puedes ejecutarlo mediante un `push` a la rama o revisar la ejecución que GitHub Actions genere para la rama.

El pipeline realiza cuatro pasos principales:

```text
Checkout
   ↓
Semgrep
   ↓
Evaluación de política
   ↓
Quality Gate
```

El escaneo genera `semgrep.json` y `security/gate.py` evalúa cada finding.

### Resultado esperado inicialmente

El pipeline debe terminar en:

```text
QUALITY GATE: FAIL
```

Esto es **intencional**.

No intentes arreglarlo todavía.

---

# 3. Analizar los findings — 8 min

Abre el reporte generado por Semgrep o revisa los logs del job.

Debes encontrar tres tipos de findings preparados para el laboratorio:

| Finding | Criticidad | Tratamiento |
|---|---:|---|
| `insurance.command-injection` | 🔴 CRITICAL | Bloquea |
| `insurance.dynamic-uri` | 🟠 HIGH | Candidato a false positive |
| `insurance.console-log` | 🟡 MEDIUM | No bloquea |

Localiza el código en:

```text
src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
```

### CRITICAL

Encontrarás una llamada a:

```java
Runtime.getRuntime().exec(command);
```

Esta alerta debe permanecer como **riesgo real** durante la primera parte del laboratorio.

### HIGH

Encontrarás la construcción de una URI:

```java
URI.create("https://risk.example/api/" + internalRiskReference);
```

La regla está diseñada para marcarla como HIGH, pero el escenario del laboratorio establece que `internalRiskReference` proviene de una **integración interna confiable** y no directamente de una petición HTTP.

Por eso debemos investigar antes de decidir.

### MEDIUM

El uso de:

```java
System.out.println(message);
```

es visible en el reporte, pero según nuestra política **no bloquea**.

---

# 4. Gestionar el false positive — 10 min

Ahora analiza el HIGH como si fueras AppSec.

## Paso 4.1 — Revisar el flujo

Pregunta:

> ¿El dato utilizado para construir la URI proviene directamente de un usuario externo?

En este ejercicio, la respuesta es **no**.

El método recibe una referencia proveniente de una integración interna confiable.

Por tanto, la regla detecta un patrón que puede ser peligroso en otros contextos, pero **no representa una vulnerabilidad en este flujo concreto**.

## Paso 4.2 — Revisar la excepción

Abre:

```text
security/false-positives.yml
```

Encontrarás una excepción como:

```yaml
exceptions:
  - rule_id: insurance.dynamic-uri
    file: src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
    reason: "The URI reference is produced by the trusted internal risk integration; this lab method does not receive HTTP request input."
    owner: "appsec-training"
    review_date: "2026-12-31"
```

Observa que la excepción tiene:

- `rule_id`
- `file`
- `reason`
- `owner`
- `review_date`

Esto representa una excepción **específica y auditable**, no una desactivación global de la regla.

---

# 5. Demostrar el efecto de la excepción — 8 min

Ahora realiza el experimento.

## Paso 5.1 — Retirar temporalmente la excepción

Elimina temporalmente del archivo `security/false-positives.yml` el bloque correspondiente a:

```text
insurance.dynamic-uri
```

Haz commit y push.

### Observa

El HIGH vuelve a aparecer como bloqueante.

El pipeline debe mostrar conceptualmente:

```text
[BLOCK] HIGH insurance.dynamic-uri ...
[BLOCK] CRITICAL insurance.command-injection ...

QUALITY GATE: FAIL
```

## Paso 5.2 — Restaurar la excepción

Vuelve a colocar la excepción.

Haz commit y push nuevamente.

Ahora deberías observar algo equivalente a:

```text
[FP] HIGH insurance.dynamic-uri ...
[REPORT] MEDIUM insurance.console-log ...
[BLOCK] CRITICAL insurance.command-injection ...

QUALITY GATE: FAIL
```

### 🎓 Punto clave

La excepción consiguió exactamente lo que queríamos:

```text
HIGH false positive → NO BLOCK
MEDIUM              → NO BLOCK
CRITICAL real       → BLOCK
```

**La excepción no apagó el análisis de seguridad.**

---

# 6. Corregir el CRITICAL — 7 min

Ahora sí debes remediar el riesgo real.

En:

```text
src/main/java/com/fictitious/insurance/securitylab/SecurityLabExamples.java
```

Elimina la ejecución directa de comandos:

```java
Runtime.getRuntime().exec(command);
```

Para este laboratorio puedes sustituir la implementación por un comportamiento seguro que **no ejecute comandos del sistema**, por ejemplo:

```java
public static String executeUserCommand(String command) {
    return "Command execution is disabled by security policy";
}
```

La finalidad de este paso no es diseñar una funcionalidad de ejecución de comandos, sino demostrar que el finding CRITICAL desaparece cuando se elimina el patrón inseguro.

Haz commit y push.

---

# 7. Re-ejecutar y cerrar — 5 min

El resultado final esperado es:

```text
=== Security Quality Gate ===

Suppressed/documented FP: 1
Non-blocking: 1
Blocking: 0

[FP] HIGH insurance.dynamic-uri ...
[REPORT] MEDIUM insurance.console-log ...

QUALITY GATE: PASS
```

El pipeline ahora pasa porque:

1. el **CRITICAL real fue corregido**;
2. el **HIGH fue demostrado y documentado como false positive**;
3. el **MEDIUM permanece visible**, pero no bloquea.

---

# 🧠 Cierre — 3 preguntas

### 1. ¿Qué habría pasado si hubiéramos hecho esto?

```yaml
continue-on-error: true
```

Respuesta esperada:

> El pipeline podría continuar aunque existiera un riesgo real. No estaríamos gestionando falsos positivos; estaríamos debilitando el Quality Gate.

### 2. ¿Por qué no desactivar la regla HIGH?

Porque la misma regla puede detectar un **true positive** en otro flujo.

La solución correcta es una excepción de **mínimo alcance**, con justificación, owner y revisión.

### 3. ¿Qué aprendimos?

```text
                 SECURITY FINDING
                        │
             ┌──────────┴──────────┐
             │                     │
       CONTEXTO REAL           CONTEXTO REAL
             │                     │
       RIESGO REAL             FALSE POSITIVE
             │                     │
          BLOCK              DOCUMENT + EXCEPT
             │                     │
             └──────────┬──────────┘
                        │
                   QUALITY GATE
```

> **DevSecOps no consiste en tener menos alertas. Consiste en conseguir que las alertas que bloquean sean realmente accionables.**

---

# 📌 Relación con la presentación

Este laboratorio está diseñado para acompañar la sección de **gestión de falsos positivos** de la presentación:

- reducción de **alert fatigue**;
- clasificación por severidad;
- análisis contextual antes de resolver un bloqueo;
- tuning de reglas;
- excepciones con trazabilidad;
- separación entre findings bloqueantes y no bloqueantes;
- mejora continua del Quality Gate.

El foco del laboratorio es deliberadamente reducido: **un escenario, una excepción y una vulnerabilidad real**, para que la demostración pueda completarse dentro de una sesión de **50 minutos** sin convertir el ejercicio en una implementación desde cero.

---

# ✅ Resultado final

Al terminar, el repositorio debe conservar:

- el escaneo SAST activo;
- la política de severidad activa;
- el false positive documentado;
- el CRITICAL corregido;
- el MEDIUM visible pero no bloqueante;
- el Quality Gate en **PASS**.

**No se debe desactivar el escaneo ni utilizar `continue-on-error` para ocultar findings.**
