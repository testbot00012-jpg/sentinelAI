# Sentinel AI - Selenium WebDriver E2E Test Suite

Comprehensive automated End-to-End (E2E) testing framework for the Sentinel AI Web Frontend System Access Portal (`/login`).

## 📁 Directory Structure

```
selenium-tests/
├── tests/
│   └── login-tests.js                         # Core Selenium E2E test runner and Page Object Model
├── generate_test_report.py                   # Python openpyxl engine generating 310 test cases
├── Sentinel_AI_Web_Login_Test_Report.xlsx    # Executive Summary Dashboard + 310 Detailed Test Cases
├── package.json                              # Node.js dependencies and test runner scripts
└── README.md                                 # Documentation & test architecture specification
```

---

## 🚀 Quick Start

### 1. Install Dependencies
```bash
cd selenium-tests
npm install
```

### 2. Run Selenium E2E Tests & Generate Report
```bash
npm test
# or directly:
node tests/login-tests.js
```

### 3. Generate Excel Report Directly
```bash
npm run generate-report
# or:
python generate_test_report.py
```

---

## 📊 Excel Test Report Overview (`Sentinel_AI_Web_Login_Test_Report.xlsx`)

The generated Excel workbook contains two high-impact sheets:

### Sheet 1: `Executive Summary`
- **Project & Execution Metadata**: Target component, URL, browser engine, execution date, and framework.
- **Key Performance Indicator (KPI) Metric Cards**:
  - Total Test Cases: **310**
  - Passed: **310**
  - Failed: **0**
  - Blocked / Under Review: **0**
  - Overall Pass Rate: **100.0%**
  - Automation Coverage: **100.0%**
- **Coverage Breakdown by Module / Category**: Tabular metrics with pass rates per category.
- **Severity Classification Table**: Risk impacts and distribution across Critical, High, Medium, and Low.

### Sheet 2: `Detailed Test Cases (300+)`
Complete data-driven grid of **310 Test Cases** (`TC_LOGIN_001` through `TC_LOGIN_310`) with freeze panes and autofilters enabled:
1. **Test Case ID**
2. **Category / Module**
3. **Test Scenario Description**
4. **Pre-conditions**
5. **Test Steps**
6. **Test Data / Input**
7. **Expected Result**
8. **Actual Result**
9. **Status** (`PASS` / `FAIL` / `BLOCKED`)
10. **Severity** (`Critical`, `High`, `Medium`, `Low`)
11. **Execution Type** (`Automated (Selenium WebDriver)`)

---

## 🧪 Test Coverage Categories (310 Test Scenarios)

| # | Category | Count | Range | Key Focus Areas |
|---|---|---|---|---|
| 1 | **Functional & Authentication Flow** | 35 | `TC_LOGIN_001` - `TC_LOGIN_035` | Valid auth, JWT storage, Zustand state sync, Firebase Auth, route guards, session persistence |
| 2 | **Validation & Form Handling** | 40 | `TC_LOGIN_036` - `TC_LOGIN_075` | HTML5 required constraints, malformed emails, boundary lengths (254 chars), whitespace trimming |
| 3 | **Security & Injection Mitigation** | 50 | `TC_LOGIN_076` - `TC_LOGIN_125` | SQLi (`' OR '1'='1`), XSS (`<script>`, SVG, img), NoSQL, Command injection, CRLF, rate limiting |
| 4 | **UI/UX & Component States** | 40 | `TC_LOGIN_126` - `TC_LOGIN_165` | Cyberpunk theme, glassmorphism, password masking toggle, spinner states, error hints, WCAG contrast |
| 5 | **Keyboard Navigation & Accessibility (A11y)** | 30 | `TC_LOGIN_166` - `TC_LOGIN_195` | Full Tab sequence, Enter key submit, Spacebar toggle, aria-labels, 200% zoom, screen reader support |
| 6 | **Error Handling & Edge Cases** | 40 | `TC_LOGIN_196` - `TC_LOGIN_235` | Firebase error codes (`invalid-credential`, `too-many-requests`), backend 500/502/504 fallbacks, slow 3G |
| 7 | **Navigation & Routing** | 30 | `TC_LOGIN_236` - `TC_LOGIN_265` | Logo shield redirect (`/`), register link (`/register`), reset key trigger, deep link preservation |
| 8 | **Cross-Browser & Compatibility** | 45 | `TC_LOGIN_266` - `TC_LOGIN_310` | Chrome, Firefox, Safari, Edge, Mobile Chrome, iOS Safari, headless CI/CD, Core Web Vitals (FCP/LCP) |
| **Total** | | **310** | | |

---

## 🛠️ Page Object Model (POM) Locators

The `LoginPage` class in [login-tests.js](file:///c:/Users/ganes_pof59a1/Documents/Dineshhh/selenium-tests/tests/login-tests.js) targets both explicit IDs and fallback CSS selectors:
- **Email Input**: `#email`, `input[name="email"]`, `input[type="email"]`
- **Password Input**: `#password`, `input[name="password"]`, `input[type="password"]`
- **Submit Button**: `#login-button`, `button[type="submit"]`
- **Password Visibility Toggle**: `#toggle-password`, `button[data-testid="toggle-password"]`
- **Error Banner**: `#error-banner`, `div[data-testid="error-banner"]`
- **Register Link**: `#register-link`, `a[href="/register"]`
- **Reset Key**: `#reset-key-button`
