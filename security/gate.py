#!/usr/bin/env python3
import json
import sys
from datetime import date

BLOCKING = {"CRITICAL", "HIGH"}
REQUIRED_EXCEPTION_FIELDS = {"rule_id", "file", "reason", "owner", "review_date"}


def load_exceptions():
    path = "security/false-positives.yml"
    exceptions = []
    current = None

    for raw_line in open(path, encoding="utf-8"):
        line = raw_line.strip()
        if line.startswith("- rule_id:"):
            if current:
                exceptions.append(current)
            current = {"rule_id": line.split(":", 1)[1].strip()}
        elif current and ":" in line:
            key, value = line.split(":", 1)
            current[key.strip()] = value.strip().strip('"')

    if current:
        exceptions.append(current)

    return exceptions


def validate_exception(item):
    missing = REQUIRED_EXCEPTION_FIELDS - set(item)
    if missing:
        raise ValueError(
            f"Invalid false-positive exception for {item.get('rule_id', 'unknown')}: "
            f"missing {', '.join(sorted(missing))}"
        )

    try:
        review_date = date.fromisoformat(item["review_date"])
    except ValueError as exc:
        raise ValueError(
            f"Invalid review_date for {item['rule_id']}: {item['review_date']}"
        ) from exc

    if review_date < date.today():
        raise ValueError(
            f"Expired false-positive exception: {item['rule_id']} ({item['review_date']})"
        )


def normalize_rule_id(rule_id):
    """Normalize Semgrep's local-config namespace when present.

    Some Semgrep execution modes expose local rule IDs with a `security.`
    namespace (for example `security.insurance.dynamic-uri`) even though the
    rule itself is declared as `insurance.dynamic-uri`. The exception registry
    intentionally stores the rule ID declared in the rule pack.
    """
    prefix = "security."
    return rule_id[len(prefix):] if rule_id.startswith(prefix) else rule_id


def main():
    report_path = sys.argv[1]
    report = json.load(open(report_path, encoding="utf-8"))

    exceptions_list = load_exceptions()
    for item in exceptions_list:
        validate_exception(item)

    exceptions = {
        (normalize_rule_id(item["rule_id"]), item["file"]): item
        for item in exceptions_list
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
        key = (normalize_rule_id(rule_id), path)

        if key in exceptions:
            suppressed.append((severity, rule_id, path))
        elif severity in BLOCKING:
            blocking.append((severity, rule_id, path))
        else:
            non_blocking.append((severity, rule_id, path))

    print("\n=== Security Quality Gate ===")
    print(f"Total findings: {len(findings)}")
    print(f"Documented false positives: {len(suppressed)}")
    print(f"Non-blocking findings: {len(non_blocking)}")
    print(f"Blocking findings: {len(blocking)}")

    for severity, rule_id, path in suppressed:
        print(f"[FP] {severity} {rule_id} {path} — documented exception")
    for severity, rule_id, path in non_blocking:
        print(f"[REPORT] {severity} {rule_id} {path}")
    for severity, rule_id, path in blocking:
        print(f"[BLOCK] {severity} {rule_id} {path}")

    if blocking:
        print("\nQUALITY GATE: FAIL")
        sys.exit(1)

    print("\nQUALITY GATE: PASS")


if __name__ == "__main__":
    main()
