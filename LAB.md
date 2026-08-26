# LAB — Métricas DevSecOps: de la herramienta al impacto

**Repositorio:** Insurance Core API  
**Duración máxima:** 50 minutos  
**Nivel:** Intermedio / avanzado  
**Modalidad:** laboratorio guiado

---

## 1. ¿Qué vamos a demostrar?

Este laboratorio acompaña la presentación **“Métricas de Adopción y DevSecOps — Early Detection • Leakage • Security • Quality • Flow”**.

La idea central de la presentación es que una herramienta instalada no demuestra que exista una capacidad DevSecOps. Hay que medir **adopción, enforcement, prevención, remediación y el impacto sobre el flow**. La presentación resume el modelo como: Adoption → Detection → Prevention → Remediation → Flow. 

En este laboratorio el pipeline ya está construido. **No vas a desarrollar una solución de métricas desde cero.** Vas a ejecutar una medición preparada, interpretar los resultados y tomar decisiones.

---

## 2. Resultado esperado

Al finalizar debes poder responder:

> **¿La seguridad está realmente integrada al delivery o solamente tenemos herramientas ejecutándose?**

Y deberías poder demostrarlo con números.

El laboratorio utiliza cinco dimensiones:

```text
┌──────────────┐
│   ADOPTION   │  ¿Está habilitado y es obligatorio?
└──────┬───────┘
       ↓
┌──────────────┐
│ EARLY        │  ¿Dónde detectamos el riesgo?
│ DETECTION    │
└──────┬───────┘
       ↓
┌──────────────┐
│   LEAKAGE    │  ¿Qué evitamos que llegue al repo?
└──────┬───────┘
       ↓
┌──────────────┐
│  SECURITY    │  ¿Qué tan rápido remediamos?
└──────┬───────┘
       ↓
┌──────────────┐
│ QUALITY/FLOW │  ¿Qué impacto tiene en delivery?
└──────────────┘
```

---

# 3. Antes de comenzar — 5 minutos

Clona el repositorio y cambia a la rama del laboratorio:

```bash
git clone https://github.com/aldo2510/insurance-core-api.git
cd insurance-core-api
git checkout exercise/devsecops-metrics
```

Revisa la estructura:

```bash
find metrics -maxdepth 2 -type f | sort
```

Encontrarás:

```text
metrics/
├── devsecops-metrics.json
└── devsecops_metrics.py
```

Y el workflow:

```text
.github/workflows/devsecops-metrics.yml
```

### Importante

Los datos son **sintéticos y reproducibles**. No necesitamos credenciales de GitHub ni APIs externas para realizar el ejercicio.

---

# 4. Ejecutar la medición — 5 minutos

Ejecuta:

```bash
python metrics/devsecops_metrics.py
```

También puedes ejecutarlo desde GitHub Actions mediante:

**Actions → DevSecOps Metrics Lab → Run workflow**

El workflow genera el mismo reporte y lo publica como artifact.

### Primera pregunta

Antes de mirar los números en detalle:

> **¿Cuál crees que es la métrica más importante: coverage, enforcement, early detection, MTTR o flow?**

No hay una respuesta única. La discusión es parte del ejercicio.

---

# 5. Adoption — 7 minutos

Busca en la salida:

```text
ADOPTION
  Coverage
  PRs with scanning
  Enforcement / gate
  PRs corrected
```

Los datos iniciales son:

```text
Repositories             10
Protected repositories     9
PRs                        50
PRs with scanning          46
PRs with gate              43
```

Calcula mentalmente:

```text
Coverage = protected repositories / repositories
Enforcement = PRs with gate / PRs
```

El script muestra aproximadamente:

```text
Coverage      90.0%
Scanning      92.0%
Enforcement   86.0%
```

### Pregunta clave

¿Por qué **90% de coverage no significa 90% de enforcement**?

La presentación insiste en esta diferencia: coverage indica dónde está habilitado el control; enforcement indica qué PR realmente está sujeto al control. fileciteturn48file14L317-L327

### Reto rápido

Imagina que mañana subimos coverage de 90% a 100%, pero enforcement permanece en 86%.

**¿Dirías que el programa llegó a 100% de adopción?**

> No. La herramienta puede estar activa sin que el control sea obligatorio.

---

# 6. Early Detection — 7 minutos

Observa:

```text
Detected in PR       37
Detected in CI        9
Escaped               4
```

El laboratorio calcula:

```text
EDR = detecciones en PR /
      (detecciones PR + CI + escaped)
```

Resultado aproximado:

```text
EDR = 74%
```

Este indicador responde:

> **¿Qué proporción del riesgo se descubre antes de llegar a producción?**

La presentación utiliza precisamente esta lectura y busca aumentar la proporción temprana y reducir escapes. fileciteturn48file9L219-L227

### Pregunta

¿Qué preferirías?

```text
A) 100 vulnerabilidades detectadas
   todas en producción

B) 100 vulnerabilidades detectadas
   90 en PR y 10 en CI
```

La respuesta correcta desde DevSecOps no es solamente “tener más detecciones”. Es **detectar antes**.

---

# 7. Leakage — 7 minutos

Ahora analiza:

```text
Detected       20
Confirmed      17
Blocked        15
Bypass          2
Exposed         0
```

La presentación propone mirar el secreto como un evento preventivo:

```text
Detectado
   ↓
Confirmado
   ↓
Bloqueado
   ↓
Bypass
   ↓
Expuesto
```

No basta con contar secretos encontrados; debemos medir cuánto evitamos que llegue al repositorio. fileciteturn48file10L238-L248

### Calcula

```text
Prevention Rate = blocked / confirmed
```

Resultado:

```text
15 / 17 = 88.2%
```

Y:

```text
Bypass Rate = bypass / confirmed
```

Resultado:

```text
2 / 17 = 11.8%
```

### Ahora mira los motivos del bypass

```text
false_positive    1
used_in_tests     1
will_fix_later    0
other             0
```

Esto es importante: **no todos los bypass tienen el mismo significado**. La presentación recomienda separar las razones para distinguir ruido, pruebas, deuda pendiente y otros casos. fileciteturn48file1L33-L38

### Pregunta

¿Te preocuparía más:

```text
10 bypass por false positive
```

o

```text
10 bypass por will-fix-later
```

¿Por qué?

---

# 8. Security — 6 minutos

Observa:

```text
Open alerts       18
Critical           1
High               4
MTTR              4.2 days
SLA               90.5%
```

La presentación plantea medir:

- backlog;
- edad de las alertas;
- MTTR;
- SLA;
- resolución.

Aquí queremos responder:

> **¿Estamos detectando problemas más rápido de lo que los estamos resolviendo?**

### Pregunta crítica

Supón que mañana:

```text
Open alerts: 18 → 8
```

pero:

```text
Critical: 1 → 3
MTTR: 4.2 → 9 días
```

¿Es una mejora?

**No necesariamente.**

El número total de alertas por sí solo puede esconder un deterioro de riesgo.

---

# 9. Quality + Flow — 7 minutos

Finalmente observa:

```text
QUALITY
  New-code coverage    84%
  Bugs                 14
  Code smells         126
  Duplication          3.2%

FLOW
  PR cycle             74 min
  Review               38 min
  Pipeline             21 min
  Security              6 min
  Lead time             19 h
  Deployment frequency  11/week
  Change failure rate   7%
```

La presentación plantea que Security, Quality y Flow deben contar una sola historia. Optimizar un vértice destruyendo otro **no es DevSecOps**. fileciteturn48file15L338-L345

### Métrica interesante

El script calcula:

```text
Security share = security time / pipeline time
```

Resultado:

```text
6 / 21 = 28.6%
```

Esto cambia la pregunta.

No preguntamos:

> “¿Security tarda 6 minutos?”

Preguntamos:

> **“¿Qué porcentaje del feedback total consume Security?”**

La presentación utiliza exactamente este enfoque para analizar Pipeline Duration. fileciteturn48file19L415-L422

---

# 10. El reto final — 5 minutos

Supón que eres responsable de DevSecOps y debes presentar el estado a un gerente.

No puedes mostrar 30 métricas.

Construye mentalmente un scorecard con cinco indicadores:

| Dimensión | Métrica elegida | Resultado |
|---|---|---:|
| Adoption | Enforcement | 86% |
| Prevention | EDR | 74% |
| Leakage | Prevention Rate | 88.2% |
| Security | MTTR | 4.2 días |
| Flow | Security share | 28.6% |

La presentación propone precisamente un **Balanced Scorecard** que combine Security, Quality y Flow. fileciteturn48file3L70-L86

### Tu misión

Explica en **60 segundos**:

> ¿Qué está funcionando, cuál es el mayor riesgo y qué deberíamos mejorar primero?

Una buena respuesta debería mencionar algo parecido a:

```text
ADOPTION
Tenemos buen coverage, pero enforcement todavía
no cubre todos los PR.

PREVENTION
La mayoría del riesgo se detecta temprano,
pero todavía existen escapes.

LEAKAGE
Push Protection evita la mayor parte de los secretos,
pero debemos revisar los bypass.

SECURITY
El MTTR está por debajo del SLA objetivo,
pero existe riesgo crítico abierto.

FLOW
Security representa casi 29% del pipeline,
por lo que debemos vigilar el costo del feedback.
```

---

# 11. Bonus — 1 cambio de datos

Si todavía tienes tiempo, modifica únicamente:

```text
metrics/devsecops-metrics.json
```

Cambia:

```json
"security_minutes": 6
```

a:

```json
"security_minutes": 12
```

Ejecuta nuevamente:

```bash
python metrics/devsecops_metrics.py
```

Observa cómo cambia:

```text
Security share
```

### Discusión

¿La seguridad empeoró?

No necesariamente.

Puede significar que:

- aumentó la profundidad del análisis;
- aumentó el tiempo de feedback;
- el pipeline está procesando más controles;
- existe una oportunidad de optimización.

Por eso **una métrica aislada no debe convertirse en un objetivo de vanidad**. La presentación recomienda mirar tendencia + severidad + contexto. fileciteturn47file5L103-L108

---

# 12. Cierre

La pregunta final del laboratorio es:

> **¿Estamos optimizando una herramienta o estamos optimizando el sistema de delivery?**

La respuesta que busca este laboratorio es la segunda.

La presentación resume el objetivo de DevSecOps de esta manera:

```text
Security
   ↓
Prevenir y reducir riesgo

Quality
   ↓
Reducir defectos y deuda

Flow
   ↓
Entregar rápido y estable
```

La métrica final no es “cuántas alertas tenemos”.

Es:

> **cuánto riesgo reducimos sin romper el flujo de ingeniería.** fileciteturn48file0L11-L22

---

## Checklist del laboratorio

- [ ] Ejecuté el reporte de métricas.
- [ ] Diferencié Coverage de Enforcement.
- [ ] Calculé EDR.
- [ ] Analicé el Leakage Funnel.
- [ ] Separé razones de bypass.
- [ ] Revisé MTTR y SLA.
- [ ] Relacioné Quality con Flow.
- [ ] Calculé el costo relativo de Security dentro del pipeline.
- [ ] Construí un scorecard ejecutivo.
- [ ] Expliqué una decisión basada en tendencia + contexto.

**Tiempo objetivo:** 45 minutos.  
**Tiempo máximo:** 50 minutos.
