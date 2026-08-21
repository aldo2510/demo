# Quality Gates + GHAS Exercise

## Objetivo

Este ejercicio muestra cómo implementar un **Quality Gate** en un Pull Request usando GitHub Actions y capacidades de GitHub Advanced Security (GHAS).

La idea es pasar de un pipeline que simplemente ejecuta comandos a un pipeline que **decide si el cambio puede continuar hacia `main`**.

## ¿Qué es un Quality Gate?

Un Quality Gate es un conjunto de criterios automáticos que un cambio debe cumplir antes de ser aceptado.

Ejemplo:

```text
Pull Request
     |
     v
+---------------------------+
|       Quality Gates       |
+---------------------------+
     |       |       |
     v       v       v
   Build  Coverage  CodeQL
     |       |       |
     +-------+-------+
             |
             v
    Dependency Review
             |
             v
       FINAL QUALITY GATE
          /         \
       PASS          FAIL
        |              |
        v              v
   Puede mergear   PR bloqueado
```

## Gates del ejercicio

| Gate | Qué valida | Resultado esperado |
|---|---|---|
| Build & Tests | Compilación y pruebas | Debe pasar |
| Checkstyle | Reglas de calidad de código | 0 errores |
| Coverage | Cobertura JaCoCo | >= 80% |
| CodeQL | Vulnerabilidades de código | Sin findings bloqueantes |
| Dependency Review | Dependencias vulnerables introducidas por el PR | Sin High/Critical |
| Final Gate | Resultado consolidado | Todos los gates deben pasar |

## Prerrequisitos

- Cuenta de GitHub con acceso al repositorio.
- Permisos para ejecutar GitHub Actions.
- GitHub Advanced Security habilitado para CodeQL si la configuración de la organización lo requiere.
- Java 25 y Maven para ejecutar el proyecto localmente.

## Paso 1 — Abrir el Pull Request

La rama del ejercicio es:

```text
exercise/quality-gates-ghas
```

El PR de ejemplo apunta a `main`.

Abre el PR y entra en la pestaña **Checks** para observar los jobs del workflow.

## Paso 2 — Ejecutar el pipeline localmente

Antes de analizar GitHub Actions, puedes comprobar el primer gate localmente:

```bash
mvn -B -ntp clean verify
```

Este comando ejecuta el ciclo de Maven utilizado por el proyecto y permite detectar errores de compilación, tests y Checkstyle.

El proyecto ya tiene configurados Checkstyle y JaCoCo en `pom.xml`.

## Paso 3 — Entender el gate de cobertura

JaCoCo genera el reporte en:

```text
target/site/jacoco/
```

El ejercicio establece como criterio:

```text
Instruction Coverage >= 80%
```

La diferencia importante es esta:

```text
Sin Quality Gate:
"Generé un reporte de cobertura."

Con Quality Gate:
"Si la cobertura es menor a 80%, el PR falla."
```

Esto transforma una métrica informativa en una condición de control.

## Paso 4 — Observar CodeQL

CodeQL analiza el código buscando patrones asociados con vulnerabilidades.

En este ejercicio el análisis se ejecuta sobre Java/Kotlin.

La finalidad didáctica es demostrar que **calidad y seguridad pueden formar parte del mismo proceso de aprobación del código**.

En GitHub revisa:

```text
Actions -> CodeQL
```

y también:

```text
Security -> Code scanning
```

## Paso 5 — Observar Dependency Review

Dependency Review analiza los cambios de dependencias introducidos por un Pull Request.

La regla del ejercicio considera bloqueantes las dependencias con severidad:

```text
High
Critical
```

La ventaja frente a revisar todas las dependencias manualmente es que el gate se concentra en el riesgo introducido por el cambio.

## Paso 6 — Provocar un fallo del Quality Gate

Ahora viene la parte importante del laboratorio.

Haz un cambio controlado que reduzca la cobertura o introduzca una violación de Checkstyle.

Por ejemplo, puedes añadir código sin pruebas en una clase existente.

Después:

```bash
git checkout -b exercise/break-quality-gate
git add .
git commit -m "test: demonstrate failing quality gate"
git push -u origin exercise/break-quality-gate
```

Abre un Pull Request hacia `main`.

Observa cómo uno de los gates falla.

El objetivo no es solamente ver el error, sino entender la consecuencia:

```text
Gate FAIL
   |
   v
Final Gate FAIL
   |
   v
PR no cumple las condiciones de calidad
```

## Paso 7 — Corregir el problema

Corrige el código, agrega o ajusta las pruebas y vuelve a ejecutar:

```bash
mvn -B -ntp clean verify
```

Después realiza commit y push:

```bash
git add .
git commit -m "fix: restore quality gate"
git push
```

GitHub Actions volverá a ejecutar los checks.

El objetivo final es obtener:

```text
Build/Test       PASS
Checkstyle       PASS
Coverage         PASS
CodeQL           PASS
Dependency       PASS
Final Gate       PASS
```

## Paso 8 — Convertir los checks en protección real

Para que el ejercicio pase de demostración a control efectivo, configura una **Branch Protection Rule** para `main`.

En GitHub:

```text
Settings
  -> Branches
  -> Branch protection rules
  -> main
```

Activa la exigencia de Pull Request y selecciona como checks obligatorios los jobs que correspondan al Quality Gate.

La idea es que GitHub no permita hacer merge si el gate requerido está en estado `failure`.

## Arquitectura recomendada

Una implementación empresarial puede evolucionar hacia:

```text
Developer
    |
    v
Pull Request
    |
    +--> Build & Unit Tests
    |
    +--> Checkstyle / Static Analysis
    |
    +--> Code Coverage
    |
    +--> CodeQL
    |
    +--> Dependency Review
    |
    v
Quality Gate
    |
    +---- FAIL ---> Fix PR
    |
    v
   PASS
    |
    v
Approved PR
    |
    v
   main
```

## Conceptos que debes explicar al presentar el ejercicio

### 1. Check no es lo mismo que Gate

Un **check** produce un resultado.

Un **gate** utiliza ese resultado para tomar una decisión.

### 2. Shift Left

CodeQL, tests, cobertura y revisión de dependencias se ejecutan durante el Pull Request, antes del despliegue.

Eso permite encontrar problemas mucho antes de producción.

### 3. Quality Gate como política

El pipeline deja de ser únicamente una automatización técnica y se convierte en una política ejecutable:

```text
"No mergear si no se cumplen estos criterios."
```

### 4. GHAS complementa la calidad

GitHub Advanced Security aporta capacidades de seguridad como CodeQL y análisis relacionado con dependencias. El ejercicio combina esas señales con calidad de código y pruebas.

## Resultado esperado del laboratorio

Al terminar deberías poder demostrar una secuencia completa:

1. Crear Pull Request.
2. Ejecutar los Quality Gates.
3. Provocar un fallo.
4. Ver el PR con un check fallido.
5. Corregir el problema.
6. Ejecutar nuevamente los checks.
7. Obtener todos los gates en `PASS`.
8. Impedir el merge cuando los checks obligatorios fallen.

Ese es el concepto central: **un Quality Gate no solamente informa; controla el flujo de entrega de software.**
