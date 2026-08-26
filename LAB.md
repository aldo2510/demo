# LAB — Métricas DevSecOps + SARIF

**Repositorio:** Insurance Core API  
**Rama:** `exercise/devsecops-metrics`  
**Duración máxima:** 50 minutos  
**Modalidad:** laboratorio guiado

## Objetivo

Este laboratorio acompaña la presentación **“Métricas de Adopción y DevSecOps — Early Detection • Leakage • Security • Quality • Flow”**.

La solución ya está preparada. El alumno **no construye el pipeline de métricas**: ejecuta el laboratorio, revisa los resultados, interpreta los indicadores y observa cómo los resultados de seguridad pueden representarse en **SARIF 2.1.0**.

> **Importante:** SARIF es el formato estructurado para resultados de herramientas de análisis. Las métricas de Adoption, Leakage, Security, Quality y Flow siguen mostrándose como reporte; los findings de seguridad se publican además como SARIF para que GitHub Code Scanning pueda visualizarlos.

---

## 1. Estructura del ejercicio

```text
metrics/
├── devsecops-metrics.json       # dataset sintético
├── devsecops_metrics.py         # cálculo de métricas
└── devsecops-results.sarif      # resultados de seguridad en SARIF 2.1.0

.github/workflows/
└── devsecops-metrics.yml        # ejecución + publicación SARIF

LAB.md                            # esta guía
```

Los datos son sintéticos y reproducibles. No se requieren credenciales ni APIs externas.

---

# 2. Ejecutar localmente — 5 min

```bash
git clone https://github.com/aldo2510/insurance-core-api.git
cd insurance-core-api
git checkout exercise/devsecops-metrics
python metrics/devsecops_metrics.py
```

El comando genera el reporte de métricas y deja disponible:

```text
metrics/devsecops-results.sarif
```

Comprueba que es JSON válido:

```bash
python -m json.tool metrics/devsecops-results.sarif > /dev/null && echo "SARIF válido"
```

---

# 3. Entender SARIF — 5 min

Abre:

```text
metrics/devsecops-results.sarif
```

Identifica las tres partes principales:

```text
SARIF
 └── runs
      ├── tool.driver
      │    ├── name
      │    └── rules
      │
      └── results
           ├── ruleId
           ├── level
           ├── message
           └── locations
```

En este laboratorio aparecen findings sintéticos como:

```text
DEVSECOPS-CRITICAL-BACKLOG   error
DEVSECOPS-HIGH-BACKLOG       error
DEVSECOPS-HIGH-COMPLEXITY    warning
```

### Pregunta

¿Por qué resulta útil un formato estándar como SARIF?

Porque permite que diferentes herramientas de seguridad entreguen resultados estructurados de forma consistente y que plataformas como GitHub puedan consumirlos.

---

# 4. Ejecutar GitHub Actions — 5 min

Ve a:

**GitHub → Actions → DevSecOps Metrics Lab → Run workflow**

El workflow realiza:

```text
Checkout
   ↓
Python
   ↓
Generación de métricas
   ↓
SARIF
   ↓
Upload SARIF
   ↓
GitHub Code Scanning
```

El workflow utiliza `github/codeql-action/upload-sarif@v3` para publicar `metrics/devsecops-results.sarif`.

También conserva el reporte y el SARIF como artifacts.

---

# 5. Adoption — 6 min

Observa el reporte:

```text
Repositories             10
Protected repositories     9
PRs                        50
PRs with scanning          46
PRs with gate              43
```

El laboratorio calcula aproximadamente:

```text
Coverage      90.0%
Scanning      92.0%
Enforcement   86.0%
```

### Pregunta

¿Por qué tener 90% de coverage no significa tener 90% de enforcement?

**Coverage** indica dónde está habilitado el control. **Enforcement** indica dónde realmente se aplica como condición del delivery.

---

# 6. Early Detection — 6 min

Observa:

```text
Detected in PR       37
Detected in CI        9
Escaped               4
EDR                  74%
```

El EDR representa la proporción del riesgo detectado temprano frente al total considerado en el ejercicio.

### Pregunta

¿Qué es preferible?

```text
100 findings detectados en producción
```

o

```text
90 detectados en PR
10 detectados en CI
```

La segunda situación representa una capacidad de prevención más temprana.

---

# 7. Leakage — 6 min

Analiza:

```text
Detected       20
Confirmed      17
Blocked        15
Bypass          2
Exposed         0
```

Calcula:

```text
Prevention Rate = 15 / 17 = 88.2%
Bypass Rate     =  2 / 17 = 11.8%
```

Los bypass tienen motivos:

```text
false_positive    1
used_in_tests     1
will_fix_later    0
other             0
```

### Pregunta

¿Un bypass por false positive tiene el mismo significado que un bypass por `will_fix_later`?

No. El contexto del bypass cambia la interpretación del riesgo y debe conservarse como dato.

---

# 8. Security + SARIF — 6 min

El reporte muestra:

```text
Open alerts       18
Critical           1
High               4
MTTR              4.2 days
SLA               90.5%
```

Ahora abre el resultado SARIF en GitHub Code Scanning.

Deberías encontrar los findings sintéticos:

| Rule | Level | Significado |
|---|---|---|
| `DEVSECOPS-CRITICAL-BACKLOG` | error | Critical abierto |
| `DEVSECOPS-HIGH-BACKLOG` | error | High abiertos |
| `DEVSECOPS-HIGH-COMPLEXITY` | warning | Riesgo de calidad |

### Pregunta clave

¿Por qué es interesante combinar el reporte de métricas con SARIF?

Porque puedes tener dos niveles:

```text
SARIF
  ↓
Finding individual

Métricas
  ↓
Tendencia / programa / impacto
```

El finding responde **“¿qué ocurrió?”**. La métrica ayuda a responder **“¿qué tan grande es el problema y está mejorando?”**.

---

# 9. Quality + Flow — 5 min

Observa:

```text
New-code coverage    84%
Bugs                 14
Code smells         126
Duplication          3.2%

PR cycle             74 min
Review               38 min
Pipeline             21 min
Security              6 min
Lead time             19 h
Deployment frequency  11/week
Change failure rate   7%
```

Calcula:

```text
Security Share = 6 / 21 = 28.6%
```

La pregunta no es solamente:

> “¿Security tarda 6 minutos?”

sino:

> **“¿Qué porcentaje del feedback total consume Security?”**

---

# 10. Reto ejecutivo — 5 min

Construye un scorecard con cinco indicadores:

| Dimensión | Indicador | Resultado |
|---|---|---:|
| Adoption | Enforcement | 86% |
| Prevention | EDR | 74% |
| Leakage | Prevention Rate | 88.2% |
| Security | MTTR | 4.2 días |
| Flow | Security Share | 28.6% |

En **60 segundos**, responde:

> ¿Qué está funcionando, cuál es el mayor riesgo y qué deberíamos mejorar primero?

Una respuesta sólida debería identificar que:

- la adopción es buena, pero todavía existe gap de enforcement;
- la mayoría del riesgo se detecta temprano, aunque existen escapes;
- push protection bloquea la mayoría de secretos confirmados;
- existe backlog crítico/high que requiere atención;
- Security consume una parte relevante del pipeline y debe vigilarse su impacto en Flow.

---

# 11. Bonus — 1 cambio de datos

Si queda tiempo, modifica en `metrics/devsecops-metrics.json`:

```json
"security_minutes": 6
```

por:

```json
"security_minutes": 12
```

Ejecuta nuevamente:

```bash
python metrics/devsecops_metrics.py
```

Observa cómo cambia **Security Share**.

### Discusión

Un indicador aislado no determina automáticamente si el programa mejoró o empeoró. Hay que interpretarlo junto con seguridad, calidad, tendencia y flow.

---

# 12. Cierre

La idea central del laboratorio es:

```text
Finding individual
       ↓
      SARIF
       ↓
Observabilidad de seguridad
       ↓
     Métricas
       ↓
Decisiones DevSecOps
```

No buscamos solamente tener herramientas ejecutándose.

Buscamos demostrar:

```text
ADOPTION
   ↓
DETECTION
   ↓
PREVENTION
   ↓
REMEDIATION
   ↓
FLOW
```

## Checklist

- [ ] Ejecuté el reporte.
- [ ] Entendí Coverage vs Enforcement.
- [ ] Calculé EDR.
- [ ] Analicé Leakage.
- [ ] Entendí el significado de los bypass.
- [ ] Abrí el SARIF en Code Scanning.
- [ ] Relacioné findings individuales con métricas agregadas.
- [ ] Calculé Security Share.
- [ ] Construí un scorecard ejecutivo.

**Tiempo objetivo:** 45 minutos  
**Tiempo máximo:** 50 minutos
