# False Positive Management — GHAS / CodeQL

## Objetivo

Este ejercicio enseña a gestionar correctamente un **false positive** detectado por una herramienta de seguridad sin convertir la excepción en una forma de desactivar el análisis.

La regla principal es:

> **No se debe silenciar un finding solamente porque bloquea el pipeline. Primero se demuestra que no aplica, se documenta la decisión y se aplica la excepción con el alcance mínimo posible.**

## ¿Qué es un falso positivo?

Un false positive ocurre cuando una herramienta reporta un posible problema de seguridad o calidad, pero después de analizar el contexto se determina que el código **no representa realmente el riesgo indicado**.

Ejemplo conceptual:

```text
CodeQL encuentra un posible riesgo
            |
            v
       ¿Es real?
        /     \
      SI       NO
      |         |
      v         v
  Corregir   Investigar
                |
                v
          Documentar evidencia
                |
                v
          Aplicar excepción
          mínima y explícita
```

## Importante: false positive != false negative

- **False positive:** la herramienta alerta, pero el riesgo no aplica.
- **False negative:** existe un riesgo, pero la herramienta no lo detecta.

Este ejercicio solamente aborda el primer caso.

## Estrategia recomendada

El proceso recomendado es:

1. Identificar el finding.
2. Entender qué regla lo generó.
3. Revisar el código y el flujo de datos.
4. Confirmar si el riesgo es real.
5. Si es real → corregirlo.
6. Si no aplica → recopilar evidencia.
7. Registrar quién tomó la decisión y por qué.
8. Aplicar una excepción con el menor alcance posible.
9. Revisar periódicamente la excepción.

## Paso 1 — Encontrar el finding

En GitHub entra en:

```text
Security
  -> Code scanning
```

Selecciona el finding y revisa:

- Rule ID
- Severidad
- Archivo
- Línea
- Mensaje
- Data flow, si está disponible
- Fecha de detección

No marques inmediatamente el finding como falso positivo.

## Paso 2 — Entender la regla

Antes de excluir una alerta, responde:

```text
¿Qué está buscando esta regla?
¿Por qué cree que este código es vulnerable?
¿Qué entrada controla el usuario?
¿Existe realmente un flujo hacia el sink peligroso?
¿Hay validación, sanitización o encoding?
¿Existe una capa de protección adicional?
```

La pregunta clave es:

> ¿Podemos demostrar técnicamente que la condición requerida por la regla no se cumple?

## Paso 3 — Clasificar el resultado

Usa una clasificación sencilla:

| Resultado | Acción |
|---|---|
| Vulnerabilidad real | Corregir |
| Finding válido pero riesgo aceptado | Registrar aceptación y seguir proceso de riesgo |
| False positive demostrado | Excepción documentada |
| No hay suficiente evidencia | Mantener abierto e investigar |

No debemos utilizar `false positive` como sinónimo de `no quiero corregirlo`.

## Paso 4 — Documentar la evidencia

Para un false positive real, registra como mínimo:

```text
Rule ID:
Archivo:
Línea:
Fecha:
Responsable:

Descripción del finding:

Por qué la herramienta lo detectó:

Análisis técnico:

Evidencia de que el riesgo no aplica:

Mitigación existente:

Decisión:

Fecha de próxima revisión:
```

### Ejemplo

```text
Rule ID: java/example-rule
Archivo: src/main/java/.../InsuranceController.java

Finding:
La herramienta identifica un posible flujo de entrada no confiable.

Análisis:
El valor señalado no llega directamente a un sink peligroso. Antes
pasa por la capa InsuranceValidator, que aplica validación estructural
permitida por el contrato de la API.

Evidencia:
- El endpoint requiere @Valid.
- El DTO restringe el formato permitido.
- InsuranceValidator rechaza valores fuera del dominio permitido.
- No existe flujo directo hacia el sink identificado.

Decisión:
False positive.

Revisión:
Revisar si cambia la implementación del validador.
```

## Paso 5 — Preferir una excepción de mínimo alcance

No hagas esto:

```text
Desactivar CodeQL completo
Desactivar toda una categoría
Ignorar todos los findings de un archivo
```

Preferir:

```text
Excepción para un finding concreto
Excepción para una regla concreta cuando sea estrictamente necesario
Excepción en el punto más pequeño posible
```

El principio es **least privilege aplicado a las excepciones de seguridad**.

## Paso 6 — ¿Cómo gestionar el false positive en GitHub?

GitHub permite gestionar los resultados de Code Scanning desde la interfaz de Security.

Dependiendo de la configuración y del tipo de análisis, un finding puede ser cerrado/clasificado con una razón apropiada.

Para este ejercicio, la clasificación debe quedar acompañada de una explicación técnica en el proceso de revisión.

La excepción no debe depender únicamente de que alguien haga clic en `Dismiss` sin dejar evidencia.

## Paso 7 — Cuando necesitas una supresión en el código

Algunas herramientas permiten utilizar mecanismos de supresión específicos.

Antes de utilizarlos, evalúa:

```text
¿La supresión aplica solamente a este finding?
¿La herramienta soporta una supresión segura y auditable?
¿Se puede explicar por qué es necesaria?
¿La supresión podría ocultar futuros problemas?
```

Evita comentarios genéricos como:

```java
// ignore security
```

Una excepción debe ser específica y justificable.

## Paso 8 — El pipeline NO debe ignorar todo

Una mala solución sería modificar el workflow para hacer algo como:

```yaml
continue-on-error: true
```

sobre CodeQL o sobre el Quality Gate completo.

Eso transforma:

```text
Security finding -> pipeline FAIL
```

en:

```text
Security finding -> pipeline PASS
```

Y destruye el objetivo del Quality Gate.

La excepción debe gestionarse en el nivel del finding o de la política, no apagando el gate entero.

## Paso 9 — Revisar las excepciones periódicamente

Una excepción de seguridad no debería convertirse en permanente por accidente.

Recomendación:

```text
Finding dismissed
       |
       v
Documented justification
       |
       v
Owner assigned
       |
       v
Review date
       |
       v
Periodic review
```

Cuando cambia el código, la arquitectura o la regla de seguridad, la excepción debe volver a evaluarse.

## Ejercicio práctico

### Escenario

Imagina que CodeQL reporta un finding en un endpoint de seguros.

El pipeline falla:

```text
CodeQL             FAIL
Final Quality Gate FAIL
PR                   BLOCKED
```

### Actividad 1 — Investigar

Determina:

- Rule ID.
- Severidad.
- Archivo y línea.
- Fuente del dato.
- Sink identificado.
- Validaciones existentes.

### Actividad 2 — Tomar una decisión

Elige una de estas opciones:

```text
A. Vulnerabilidad real -> corregir
B. False positive -> documentar y gestionar excepción
C. Riesgo aceptado -> seguir proceso formal de riesgo
D. Evidencia insuficiente -> mantener finding abierto
```

### Actividad 3 — Demostrar el cambio

Después de aplicar la solución:

```text
CodeQL
   |
   v
Re-run
   |
   +---- finding corregido
   |
   +---- finding gestionado correctamente
   |
   v
Final Quality Gate
   |
   v
PASS
```

## Buenas prácticas empresariales

### 1. No permitir auto-dismiss indiscriminado

Las excepciones deberían tener trazabilidad.

### 2. Separar seguridad de conveniencia

No clasificar un finding como false positive únicamente porque corregirlo requiere trabajo.

### 3. Tener ownership

Cada excepción debería tener un responsable técnico o equipo responsable.

### 4. Usar expiración o revisión

Cuando sea posible, establecer una fecha de revisión.

### 5. Medir excepciones

Puedes medir:

```text
Total findings
Total false positives
False positive rate
Open exceptions
Expired exceptions
Exceptions by team
Exceptions by severity
```

### 6. Revisar especialmente High/Critical

Un finding de alta severidad necesita evidencia mucho más fuerte antes de ser descartado.

## Qué NO hacer

❌ Desactivar CodeQL.

❌ Ignorar todos los findings de una carpeta.

❌ Añadir `continue-on-error: true` al análisis de seguridad.

❌ Marcar todo como false positive para conseguir un pipeline verde.

❌ Copiar una supresión sin entender la regla.

❌ Mantener excepciones sin owner ni revisión.

## Modelo de gobierno recomendado

```text
Developer
   |
   v
Finding
   |
   v
Security/Engineering Review
   |
   +--> Real vulnerability ----> Fix
   |
   +--> False positive --------> Evidence + Exception
   |
   +--> Risk accepted ---------> Risk process
   |
   v
Quality Gate
   |
   v
Merge
```

## Resultado esperado

Al terminar este laboratorio debes poder explicar:

1. Qué es un false positive.
2. Cómo distinguirlo de una vulnerabilidad real.
3. Cómo investigar un finding de CodeQL.
4. Qué evidencia necesitas para descartarlo.
5. Por qué no se debe apagar CodeQL.
6. Cómo aplicar una excepción de mínimo alcance.
7. Cómo mantener trazabilidad y ownership.
8. Por qué las excepciones deben revisarse periódicamente.

### Regla de oro

> **Primero demostrar que no es vulnerable; después gestionar la excepción. Nunca al revés.**
