#!/usr/bin/env python3
"""
Sentinel AI Master Security Suite Runner
Orchestrates:
1. Automatic backend technology detection
2. Static Application Security Testing (SAST)
3. Dependency Vulnerability Analysis (SCA)
4. Dynamic API Security Checks (DAST)
5. Report and Spreadsheet Generation
6. GitHub Actions Summary output
7. Exit code enforcement (fails only on Critical vulnerabilities)
"""

import os
import sys
import json
import argparse
import subprocess

workspace_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
sys.path.insert(0, workspace_root)

from security_tools.tech_detector import detect_backend_technology
from security_tools.generate_reports import generate_markdown_reports, generate_excel_workbooks, FINDINGS
from security_tools.dast_scanner import run_all_security_tests

def main():
    parser = argparse.ArgumentParser(description="Sentinel AI Universal Backend Security Audit Suite")
    parser.add_argument("--fail-on-critical", action="store_true", default=True, help="Fail CI with exit code 1 if Critical vulnerabilities are detected")
    parser.add_argument("--github-summary", action="store_true", default=False, help="Write markdown summary to GITHUB_STEP_SUMMARY environment file")
    args = parser.parse_args()

    print("=" * 70)
    print("SENTINEL AI UNIVERSAL BACKEND SECURITY AUDIT SUITE")
    print("=" * 70)

    # 1. Automatic Technology Detection
    print("\n[PHASE 1] Detecting Backend Technology...")
    tech = detect_backend_technology(workspace_root)
    print(f"  -> Language:        {tech['language']}")
    print(f"  -> Framework:       {tech['framework']}")
    print(f"  -> Package Manager: {tech['package_manager']}")
    print(f"  -> Target Directory:{tech['backend_dir']}")
    print(f"  -> Confidence:      {tech['confidence']*100:.0f}%")

    # 2. SAST Scan
    print("\n[PHASE 2] Running Static Application Security Testing (SAST)...")
    sast_issues = []
    if tech['language'] == "Python":
        try:
            cmd = ["bandit", "-r", os.path.join(workspace_root, "backend", "app"), "-f", "json", "-q"]
            res = subprocess.run(cmd, capture_output=True, text=True)
            if res.stdout:
                data = json.loads(res.stdout)
                sast_issues = data.get("results", [])
                print(f"  -> Bandit SAST completed. Identified {len(sast_issues)} code pattern alerts.")
        except Exception as e:
            print(f"  -> Bandit note: {e}")

    # 3. Dynamic API Security Testing (DAST)
    print("\n[PHASE 3] Running Dynamic Application Security Testing (DAST)...")
    dast_results = run_all_security_tests()
    summary = dast_results.get("summary", {})
    print(f"  -> Total DAST Assertions: {summary.get('total_tests', 0)}")
    print(f"  -> Passed:                {summary.get('passed', 0)}")
    print(f"  -> Failed (Vulnerabilities): {summary.get('failed', 0)}")
    print(f"  -> Pass Rate:             {summary.get('pass_rate_percent', 0)}%")

    # 4. Generate Reports & Excel
    print("\n[PHASE 4] Generating Security Review Deliverables...")
    generate_markdown_reports()
    generate_excel_workbooks()
    print("  -> Saved to 'Vulnerability Test Results/':")
    print("     - security-review.md")
    print("     - executive-summary.md")
    print("     - dependency-report.md")
    print("     - endpoint-inventory.xlsx")
    print("     - findings.xlsx (4 Sheets)")

    # 5. Severity Counts
    crit_count = len([v for v in FINDINGS if v['severity'] == 'Critical'])
    high_count = len([v for v in FINDINGS if v['severity'] == 'High'])
    med_count = len([v for v in FINDINGS if v['severity'] == 'Medium'])
    low_count = len([v for v in FINDINGS if v['severity'] == 'Low'])
    total_findings = len(FINDINGS)
    raw_deduction = (crit_count * 15) + (high_count * 7) + (med_count * 3) + (low_count * 1)
    score = max(15, 100 - raw_deduction)

    print("\n" + "=" * 70)
    print(f"SECURITY POSTURE SCORE: {score} / 100")
    print(f"Critical: {crit_count} | High: {high_count} | Medium: {med_count} | Low: {low_count}")
    print("=" * 70)

    # 6. Publish GitHub Action Summary if running in CI
    summary_file = os.getenv("GITHUB_STEP_SUMMARY")
    if summary_file and (args.github_summary or os.path.exists(summary_file)):
        try:
            with open(summary_file, "a", encoding="utf-8") as gf:
                gf.write("## 🛡️ Sentinel AI Backend Security Audit Summary\n\n")
                gf.write(f"**Detected Backend**: {tech['language']} ({tech['framework']})  \n")
                gf.write(f"**Overall Security Score**: **{score} / 100**  \n")
                gf.write(f"**Total Security Assertions Run**: {summary.get('total_tests', 0)} ({summary.get('pass_rate_percent', 0)}% pass rate)\n\n")
                
                gf.write("### 📊 Findings by Severity\n\n")
                gf.write("| Severity | Count | Status |\n")
                gf.write("|---|---|---|\n")
                gf.write(f"| 🚨 **Critical** | **{crit_count}** | {'❌ Failing Build' if crit_count > 0 else '✅ Clear'} |\n")
                gf.write(f"| ⚠️ **High** | **{high_count}** | Review Required |\n")
                gf.write(f"| 🟡 **Medium** | **{med_count}** | Scheduled Fix |\n")
                gf.write(f"| 🟢 **Low** | **{low_count}** | Informational |\n\n")
                
                gf.write("### 🚨 Critical Security Vulnerabilities Requiring Immediate Remediation\n\n")
                for f in FINDINGS:
                    if f['severity'] == 'Critical':
                        gf.write(f"- **{f['id']}: {f['title']}** (`{f['file_path']}`)\n")
                        gf.write(f"  - *Impact*: {f['impact']}\n")
                        gf.write(f"  - *Fix*: {f['remediation']}\n\n")
                
                gf.write("📁 *Detailed markdown and Excel workbooks are uploaded in the workflow artifacts.* \n")
        except Exception as ex:
            print(f"  -> Warning writing GITHUB_STEP_SUMMARY: {ex}")

    # 7. Fail only on Critical vulnerabilities
    if args.fail_on_critical and crit_count > 0:
        print(f"\n[CI ENFORCEMENT] Build failed due to {crit_count} CRITICAL security vulnerabilities.")
        sys.exit(1)
    else:
        print("\n[CI ENFORCEMENT] Passed: No unhandled Critical blocking vulnerabilities.")
        sys.exit(0)

if __name__ == "__main__":
    main()
