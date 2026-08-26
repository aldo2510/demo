#!/usr/bin/env python3
import json
import sys

BLOCKING = {"CRITICAL", "HIGH"}
ORDER = {"LOW": 1, "MEDIUM": 2, "HIGH": 3, "CRITICAL": 4}


def main():
    report_path = sys.argv[1]
    report = json.load(open(report_path, encoding="utf-8"))
    exceptions = {
        (item["rule_id"], item["file"]): item
        for item in load_exceptions().get("exceptions", [])
    }

    findings = report.get("results", [])
    blocking = []
    suppressed = []
    non_blocking = []

    for finding in findings:
        extra = finding.get("extra", {})
        metadata = extra.get("metadata", {})
        severity = str(metadata.get("severity", "MEDIUM")).upper()
        rule_id = finding.get("check_id", "unknown")
        path = finding.get("path", "unknown")
        key = (rule_id, path)

        if key in exceptions:
            suppressed.append(f"{severity} {rule_id} {path} — documented false positive")
        elif severity in BLOCKING:
            blocking.append((severity, rule_id, path))
        else:
            non_blocking.append((severity, rule_id, path))

    print("\n=== Security Quality Gate ===")
    print(f"Total findings: {len(findings)}")
    print(f"Suppressed/documented FP: {len(suppressed)}")
    print(f"Non-blocking: {len(non_blocking)}")
    print(f"Blocking: {len(blocking)}")

    for item in suppressed:
        print(f"[FP] {item}")
    for severity, rule_id, path in non_blocking:
        print(f"[REPORT] {severity} {rule_id} {path}")
    for severity, rule_id, path in blocking:
        print(f"[BLOCK] {severity} {rule_id} {path}")

    if blocking:
        print("\nQUALITY GATE: FAIL")
        sys.exit(1)

    print("\nQUALITY GATE: PASS")


def load_exceptions():
    import re
    path = "security/false-positives.yml"
    data = {"exceptions": []}
    current = None
    for line in open(path, encoding="utf-8"):
        line = line.strip()
        if line.startswith("- rule_id:"):
            if current:
                data["exceptions"].append(current)
            current = {"rule_id": line.split(":", 1)[1].strip()}
        elif current and ":" in line:
            key, value = line.split(":", 1)
            current[key.strip()] = value.strip().strip('"')
    if current:
        data["exceptions"].append(current)
    return data


if __name__ == "__main__":
    main()
