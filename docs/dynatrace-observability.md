# Dynatrace pipeline observability POC

This branch contains a small GitHub Actions proof of concept that publishes CI/CD telemetry to Dynatrace.

## What is sent

The workflow `.github/workflows/dynatrace-observability.yml` publishes:

### SDLC events

Two events are sent to the Dynatrace SDLC Events endpoint:

- `pipeline / started` at the beginning of the job.
- `pipeline / finished` after Maven verification.

The events include the repository, branch, commit, GitHub Actions run ID, workflow name and workflow URL.

### Custom metrics

The workflow also sends these custom metrics through the Dynatrace Metrics API:

- `insurance.pipeline.duration.seconds`
- `insurance.pipeline.success`
- `insurance.pipeline.run.count`
- `insurance.pipeline.maven.verify.success`

The dimensions intentionally exclude the GitHub run ID to avoid creating a high-cardinality time series for every execution.

## GitHub secrets

Configure these repository secrets:

- `DYNATRACE_ENV_URL` — for example `https://<environment-id>.live.dynatrace.com`
- `DYNATRACE_API_TOKEN` — Dynatrace access token used only for telemetry ingestion

For this POC the token needs:

- `openpipeline.events_sdlc`
- `metrics.ingest`

Do not commit the token or place it directly in workflow YAML.

## DQL examples

### Pipeline events

```dql
fetch events, from:now()-1h
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| sort start_time desc
```

### Pipeline executions

```dql
fetch events, from:now()-1d
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| summarize runs=count(), by:{pipeline.name, pipeline.status}
```

### Pipeline duration from SDLC events

```dql
fetch events, from:now()-7d
| filter event.kind == "SDLC_EVENT"
| filter event.type == "pipeline"
| filter event.status == "finished"
| fieldsAdd duration = toTimestamp(end_time) - toTimestamp(start_time)
| summarize avg_duration=avg(duration), p90_duration=percentile(duration, 90), by:{pipeline.name}
```

### Custom pipeline duration metric

```dql
timeseries avg_duration=avg(insurance.pipeline.duration.seconds),
  by:{repository, branch, workflow}
```

## Demo flow

1. Add the two GitHub secrets.
2. Push a change to `feature/dynatrace-observability` or manually run the workflow.
3. Open Dynatrace Notebooks and run the DQL queries above.
4. Use the resulting SDLC events as the source for a pipeline dashboard.
5. Use the custom metrics for duration, success rate and execution volume.

Telemetry failures are intentionally non-blocking for this POC: the application build remains the source of truth for pipeline success.
