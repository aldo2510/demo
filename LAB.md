# LAB — Dynatrace Pipeline Observability

## Objetivo

Construir en Dynatrace un dashboard de observabilidad para ejecuciones de CI/CD, utilizando los eventos `SDLC_EVENT` generados por el pipeline.

Al finalizar el laboratorio, el dashboard debe permitir visualizar de forma rápida:

- Cantidad de ejecuciones de pipelines.
- Tendencia de ejecuciones en el tiempo.
- Tiempo promedio de ejecución.
- Ratio de éxito de los pipelines.
- Detalle de cada ejecución, incluyendo información del pipeline y su ejecución.

## Contexto

En este ejercicio se utilizan eventos de tipo `pipeline` almacenados como `SDLC_EVENT`.

Los eventos de una ejecución contienen información como:

- `event.type`: tipo de evento (`pipeline`).
- `event.status`: estado del evento (`started` / `finished`).
- `pipeline.status`: resultado de la ejecución (`success`, etc.).
- `pipeline.id`: identificador de la ejecución.
- `pipeline.name`: nombre del pipeline.
- `pipeline.url`: enlace a la ejecución del pipeline.
- `vcs.repository`: repositorio asociado.
- `vcs.branch`: rama utilizada.
- `start_time`: inicio de la ejecución.
- `end_time`: fin de la ejecución.

> **Nota:** El laboratorio parte de que los eventos ya están llegando correctamente a Dynatrace. Si en Logs aparece `Logs ingestion isn't set up yet`, primero debe configurarse la ingesta de logs/eventos.

---

## 1. Validar los eventos del pipeline

En **Dynatrace → Notebooks** crear una sección DQL y ejecutar:

```dql
fetch events
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| sort timestamp desc
| limit 50
```

Se deben observar los eventos de las ejecuciones del pipeline.

Para analizar únicamente las ejecuciones finalizadas:

```dql
fetch events
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| filter event.status == "finished"
| sort timestamp desc
| limit 50
```

---

## 2. Crear métrica de ejecuciones

Para contar las ejecuciones reales, utilizar los eventos `finished`:

```dql
fetch events
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| filter event.status == "finished"
| summarize executions = count()
```

### Visualización recomendada

**Single value / KPI**.

Título sugerido:

> **EXECUTIONS**

---

## 3. Crear tendencia de ejecuciones

Para visualizar cómo se distribuyen las ejecuciones en el tiempo:

```dql
fetch events
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| filter event.status == "finished"
| summarize executions = count(), by: { bin(timestamp, 5m) }
| sort timestamp asc
```

### Visualización recomendada

**Line chart** o **bar chart**.

Título sugerido:

> **Pipeline Executions Over Time**

---

## 4. Calcular duración de las ejecuciones

La duración se obtiene a partir de `end_time - start_time`.

```dql
fetch events
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| filter event.status == "finished"
| fieldsAdd duration = toTimestamp(end_time) - toTimestamp(start_time)
| summarize avg_duration = avg(duration)
```

### Visualización recomendada

**Single value / KPI**.

Título sugerido:

> **AVERAGE DURATION**

El valor debe mostrarse en segundos cuando la duración sea pequeña.

---

## 5. Calcular el ratio de éxito

El ratio de éxito se calcula sobre las ejecuciones finalizadas, utilizando `pipeline.status`.

```dql
fetch events
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| filter event.status == "finished"
| summarize
    total_pipelines = count(),
    successful_pipelines = countIf(pipeline.status == "success")
| fieldsAdd success_rate = 100.0 * successful_pipelines / total_pipelines
```

### Visualización recomendada

**Single value / KPI**.

Título sugerido:

> **SUCCESS RATE**

Formatear el valor como porcentaje.

---

## 6. Crear el detalle de ejecuciones

Agregar una tabla para poder identificar qué pipelines se ejecutaron.

```dql
fetch events
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| filter event.status == "finished"
| fields
    timestamp,
    pipeline.id,
    pipeline.name,
    pipeline.status,
    pipeline.run.id,
    pipeline.url,
    vcs.repository,
    vcs.branch
| sort timestamp desc
| limit 50
```

### Visualización recomendada

**Table**.

La URL del pipeline puede utilizarse para navegar directamente hacia la ejecución correspondiente.

---

## 7. Dashboard final

Crear un dashboard llamado:

> **Dynatrace Pipeline Observability - POC**

Se recomienda una distribución similar a la siguiente:

```text
┌───────────────────┬───────────────────┬──────────────────────┐
│    EXECUTIONS     │   EXECUTIONS      │  AVERAGE DURATION    │
│         9         │   OVER TIME       │        13 s           │
└───────────────────┴───────────────────┴──────────────────────┘

┌──────────────────────────────────────┬──────────────────────┐
│                                      │                      │
│       PIPELINE EXECUTION TABLE       │    SUCCESS RATE       │
│                                      │       100.00 %        │
│                                      │                      │
└──────────────────────────────────────┴──────────────────────┘
```

El resultado de referencia del laboratorio muestra:

- **9 executions**.
- **13 s** de duración promedio.
- **100.00 %** de ratio de éxito.
- Tabla con el detalle de las ejecuciones.

> Los valores anteriores son los observados en el dashboard de referencia y pueden variar según el número de ejecuciones realizadas y el timeframe seleccionado.

---

## 8. Resultado esperado

El dashboard debe permitir responder rápidamente preguntas como:

1. ¿Cuántas veces se ejecutó el pipeline?
2. ¿Con qué frecuencia se están ejecutando los pipelines?
3. ¿Cuánto tiempo tarda, en promedio, una ejecución?
4. ¿Qué porcentaje de ejecuciones termina correctamente?
5. ¿Qué repositorio y rama generaron una ejecución?
6. ¿Cuál fue el resultado de una ejecución específica?
7. ¿Dónde puedo abrir directamente la ejecución del pipeline?

---

## Buenas prácticas

- Utilizar `event.status == "finished"` para evitar contar dos veces una misma ejecución (`started` + `finished`).
- Utilizar `pipeline.status` para determinar el resultado de la ejecución.
- Mantener un timeframe consistente entre los diferentes tiles del dashboard.
- Mostrar el dashboard con un timeframe suficientemente amplio para observar tendencia, por ejemplo **Last 2 hours**, **Last 24 hours** o el período definido para el laboratorio.
- Mantener una tabla de detalle junto con los KPIs para facilitar el troubleshooting.

## Entregable

Al completar el laboratorio se debe contar con un dashboard funcional de **Pipeline Observability** en Dynatrace que consolide ejecución, duración, éxito y detalle de pipelines en una sola vista.