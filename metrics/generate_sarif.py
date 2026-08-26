#!/usr/bin/env python3
"""Generate a deterministic SARIF report for the DevSecOps metrics lab."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "devsecops-results.sarif"

results = [
    {
        "ruleId": "DEVSECOPS-CRITICAL-BACKLOG",
        "level": "error",
        "message": {"text": "Critical security backlog item requires immediate remediation."},
        "locations": [{"physicalLocation": {"artifactLocation": {"uri": "metrics/devsecops-metrics.json"}, "region": {"startLine": 1, "startColumn": 1}}}],
        "properties": {"severity": "CRITICAL", "category": "security", "metric": "open_alerts"},
    },
    {
        "ruleId": "DEVSECOPS-HIGH-BACKLOG",
        "level": "error",
        "message": {"text": "High-severity security backlog requires remediation within SLA."},
        "locations": [{"physicalLocation": {"artifactLocation": {"uri": "metrics/devsecops-metrics.json"}, "region": {"startLine": 1, "startColumn": 1}}}],
        "properties": {"severity": "HIGH", "category": "security", "metric": "high_open"},
    },
    {
        "ruleId": "DEVSECOPS-HIGH-COMPLEXITY",
        "level": "warning",
        "message": {"text": "High-complexity items increase maintenance and security risk."},
        "locations": [{"physicalLocation": {"artifactLocation": {"uri": "metrics/devsecops-metrics.json"}, "region": {"startLine": 1, "startColumn": 1}}}],
        "properties": {"severity": "MEDIUM", "category": "quality", "metric": "high_complexity_items"},
    },
]

rules = [
    {"id": "DEVSECOPS-CRITICAL-BACKLOG", "name": "Critical Security Backlog", "shortDescription": {"text": "Critical security backlog"}, "defaultConfiguration": {"level": "error"}},
    {"id": "DEVSECOPS-HIGH-BACKLOG", "name": "High Security Backlog", "shortDescription": {"text": "High security backlog"}, "defaultConfiguration": {"level": "error"}},
    {"id": "DEVSECOPS-HIGH-COMPLEXITY", "name": "High Complexity", "shortDescription": {"text": "High-complexity quality item"}, "defaultConfiguration": {"level": "warning"}},
]

sarif = {
    "$schema": "https://json.schemastore.org/sarif-2.1.0.json",
    "version": "2.1.0",
    "runs": [{
        "tool": {"driver": {"name": "DevSecOps Metrics Lab", "version": "1.0.0", "rules": rules}},
        "results": results,
    }],
}

OUTPUT.write_text(json.dumps(sarif, indent=2) + "\n", encoding="utf-8")
print(f"Generated SARIF: {OUTPUT}")
print(f"Results: {len(results)}")
