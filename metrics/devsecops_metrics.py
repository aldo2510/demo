#!/usr/bin/env python3
"""Deterministic DevSecOps metrics exercise.

The dataset is intentionally small and synthetic so the lab can run without
GitHub APIs, credentials, or external services.
"""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
DATA = ROOT / "devsecops-metrics.json"


def pct(numerator, denominator):
    return (numerator / denominator * 100) if denominator else 0.0


def load():
    return json.loads(DATA.read_text(encoding="utf-8"))


def main():
    d = load()
    p = d["program"]
    ed = d["early_detection"]
    le = d["leakage"]
    sec = d["security"]
    q = d["quality"]
    f = d["flow"]
    br = d["bypass_reasons"]

    coverage = pct(p["protected_repositories"], p["repositories"])
    scanning = pct(p["prs_with_scanning"], p["prs"])
    enforcement = pct(p["prs_with_gate"], p["prs"])
    corrected = pct(p["prs_corrected"], p["prs"])
    edr = pct(ed["detected_in_pr"], ed["detected_in_pr"] + ed["detected_in_ci"] + ed["escaped_to_production"])
    leakage_prevention = pct(le["blocked_by_push_protection"], le["confirmed_secrets"])
    bypass_rate = pct(le["bypassed"], le["confirmed_secrets"])
    sla = pct(sec["resolved_within_sla"], sec["resolved_total"])
    security_share = pct(f["security_minutes"], f["pipeline_minutes"])
    fp_bypass = pct(br["false_positive"], sum(br.values()))

    print("=" * 64)
    print("DEVSECOPS METRICS — INSURANCE CORE API")
    print("=" * 64)
    print("\nADOPTION")
    print(f"  Coverage             {coverage:5.1f}%  ({p['protected_repositories']}/{p['repositories']} repos)")
    print(f"  PRs with scanning    {scanning:5.1f}%  ({p['prs_with_scanning']}/{p['prs']})")
    print(f"  Enforcement / gate   {enforcement:5.1f}%  ({p['prs_with_gate']}/{p['prs']})")
    print(f"  PRs corrected        {corrected:5.1f}%  ({p['prs_corrected']}/{p['prs']})")

    print("\nEARLY DETECTION")
    print(f"  Detected in PR       {ed['detected_in_pr']:>3}")
    print(f"  Detected in CI       {ed['detected_in_ci']:>3}")
    print(f"  Escaped              {ed['escaped_to_production']:>3}")
    print(f"  EDR                  {edr:5.1f}%")

    print("\nLEAKAGE")
    print(f"  Detected             {le['detected_secrets']:>3}")
    print(f"  Confirmed            {le['confirmed_secrets']:>3}")
    print(f"  Blocked              {le['blocked_by_push_protection']:>3}")
    print(f"  Bypass               {le['bypassed']:>3}  ({bypass_rate:.1f}%)")
    print(f"  Exposed              {le['exposed']:>3}")
    print(f"  Prevention rate      {leakage_prevention:5.1f}%")
    print("  Bypass reasons")
    for reason, count in br.items():
        print(f"    - {reason:17} {count}")

    print("\nSECURITY")
    print(f"  Open alerts          {sec['open_alerts']:>3}")
    print(f"  Critical / High      {sec['critical_open']:>3} / {sec['high_open']}")
    print(f"  Average age          {sec['average_age_days']:.1f} days")
    print(f"  MTTR                 {sec['mttr_days']:.1f} days")
    print(f"  SLA                  {sla:5.1f}%  (target <= {sec['sla_target_days']} days)")

    print("\nQUALITY")
    print(f"  New-code coverage    {q['coverage_new_code_pct']:.1f}%")
    print(f"  Bugs                 {q['bugs']}")
    print(f"  Code smells          {q['code_smells']}")
    print(f"  Duplication          {q['duplication_pct']:.1f}%")
    print(f"  High complexity      {q['high_complexity_items']}")

    print("\nFLOW")
    print(f"  PR cycle             {f['pr_cycle_minutes']} min")
    print(f"  Review               {f['review_minutes']} min")
    print(f"  Pipeline             {f['pipeline_minutes']} min")
    print(f"  Security share       {security_share:.1f}% of pipeline")
    print(f"  Security             {f['security_minutes']} min")
    print(f"  Lead time            {f['lead_time_hours']} h")
    print(f"  Deployment frequency {f['deployment_frequency_per_week']}/week")
    print(f"  Change failure rate  {f['change_failure_rate_pct']:.1f}%")

    print("\nEXECUTIVE READOUT")
    print(f"  Adoption: {coverage:.0f}% coverage, {enforcement:.0f}% enforcement")
    print(f"  Prevention: {edr:.0f}% early detection, {leakage_prevention:.0f}% secret prevention")
    print(f"  Remediation: {sla:.0f}% within SLA, MTTR {sec['mttr_days']:.1f}d")
    print(f"  Flow: security consumes {security_share:.0f}% of pipeline time")
    print(f"  Bypass FP share: {fp_bypass:.0f}% of bypass reasons")
    print("=" * 64)


if __name__ == "__main__":
    main()
