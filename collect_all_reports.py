#!/usr/bin/env python3
"""
Sentinel AI Unified Test Deliverables Collector
Collects and indexes all Excel reports from all 4 test suites:
1. Selenium Web E2E Tests (310+ test cases)
2. Appium Mobile E2E Tests (315+ test cases)
3. Security Review & Penetration Tests (4-sheet findings & endpoint inventory)
4. Baseline Load & Concurrency Tests (100 users, 60s performance metrics)
"""

import os
import shutil
from datetime import datetime

workspace_root = os.path.dirname(os.path.abspath(__file__))
dest_dir = os.path.join(workspace_root, "Test-Reports-Excel")
os.makedirs(dest_dir, exist_ok=True)

REPORTS = [
    {
        "suite": "1. Selenium Web E2E Testing",
        "src": os.path.join(workspace_root, "selenium-tests", "Sentinel_AI_Web_Login_Test_Report.xlsx"),
        "dest_name": "1_Web_Selenium_Login_Tests_Report.xlsx",
        "desc": "310 E2E functional, security, boundary, and UI test cases for Web Frontend."
    },
    {
        "suite": "2. Appium Mobile E2E Testing",
        "src": os.path.join(workspace_root, "appium-tests", "Sentinel_AI_Android_Appium_Test_Report.xlsx"),
        "dest_name": "2_Mobile_Appium_Android_Tests_Report.xlsx",
        "desc": "315 Mobile automation test cases covering navigation, permissions, auditor, and scans."
    },
    {
        "suite": "3. Security Review Findings",
        "src": os.path.join(workspace_root, "Vulnerability Test Results", "findings.xlsx"),
        "dest_name": "3_Security_Vulnerability_Findings.xlsx",
        "desc": "Complete 4-sheet security audit workbook (Findings, Endpoints, Dependencies, Risk Summary)."
    },
    {
        "suite": "3. Security Endpoint Inventory",
        "src": os.path.join(workspace_root, "Vulnerability Test Results", "endpoint-inventory.xlsx"),
        "dest_name": "3_Security_Endpoint_Inventory.xlsx",
        "desc": "Complete API catalog with HTTP verbs, authentication, roles, and risk classifications."
    },
    {
        "suite": "4. Baseline Load & Concurrency Testing",
        "src": os.path.join(workspace_root, "load-tests", "load-test-results.xlsx"),
        "dest_name": "4_Baseline_Load_Performance_Tests.xlsx",
        "desc": "4-sheet load test report for 100 concurrent users over 60s (RPS, Timeline, Percentiles, Endpoints)."
    }
]

def collect_reports():
    print("=" * 70)
    print("COLLECTING ALL 4 TEST EXCEL REPORTS")
    print("=" * 70)
    
    copied = []
    for rep in REPORTS:
        src = rep["src"]
        if os.path.exists(src):
            dst = os.path.join(dest_dir, rep["dest_name"])
            shutil.copy2(src, dst)
            size_kb = os.path.getsize(dst) / 1024
            print(f" [COPIED] {rep['dest_name']} ({size_kb:.1f} KB)")
            copied.append({**rep, "size_kb": size_kb})
        else:
            print(f" [MISSING] Source file not found: {src}")

    # Generate Catalog README inside the artifact folder
    readme_path = os.path.join(dest_dir, "README.md")
    with open(readme_path, "w", encoding="utf-8") as f:
        f.write("# Sentinel AI - Unified Test Deliverables (All 4 Test Suites)\n\n")
        f.write(f"**Generated**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}  \n")
        f.write("**Target Architecture**: Sentinel AI Platform (Web, Android, Backend, DevSecOps)\n\n")
        f.write("This directory packages all official Excel (`.xlsx`) test reports across all 4 verification suites.\n\n")
        f.write("## Index of Downloadable Excel Reports\n\n")
        f.write("| Suite # | Test Category | Excel File Name | Size | Scope & Description |\n")
        f.write("|---|---|---|---|---|\n")
        for c in copied:
            f.write(f"| **{c['suite'].split('.')[0]}** | {c['suite'].split('. ')[1]} | **`{c['dest_name']}`** | {c['size_kb']:.1f} KB | {c['desc']} |\n")
        f.write("\n---\n")
        f.write("### Excel Sheets Overview\n")
        f.write("1. **`1_Web_Selenium_Login_Tests_Report.xlsx`**: Detailed test logs for 310 web test cases including authentication, validation, state management, and edge cases.\n")
        f.write("2. **`2_Mobile_Appium_Android_Tests_Report.xlsx`**: Detailed test logs for 315 mobile automation cases covering permissions, biometric fallbacks, auditor, and UI gestures.\n")
        f.write("3. **`3_Security_Vulnerability_Findings.xlsx`**: 4 Sheets: Security Findings, Endpoint Inventory, Dependency Vulnerabilities, and Risk Summary Score.\n")
        f.write("4. **`3_Security_Endpoint_Inventory.xlsx`**: Complete API inventory of all public and protected routes with parameter definitions.\n")
        f.write("5. **`4_Baseline_Load_Performance_Tests.xlsx`**: 4 Sheets: Executive Summary & KPIs, Endpoint Breakdown, 60s Second-by-Second Timeline, and Latency Percentiles.\n")

    print("-" * 70)
    print(f"Successfully collected {len(copied)} Excel test workbooks into: '{dest_dir}'")
    print("=" * 70)

if __name__ == "__main__":
    collect_reports()
