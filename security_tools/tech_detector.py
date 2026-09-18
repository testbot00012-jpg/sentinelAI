#!/usr/bin/env python3
"""
Sentinel AI Universal Backend Technology Detector
Automatically inspects the repository root and subdirectories to detect the backend
framework, language, package manager, and route configuration.
Works for Python (FastAPI/Flask/Django), Node.js (Express/NestJS), Java (Spring Boot),
Go, Ruby on Rails, PHP (Laravel), ASP.NET Core, and generic REST frameworks.
"""

import os
import json
import glob
from typing import Dict, Any, List

def detect_backend_technology(workspace_root: str = ".") -> Dict[str, Any]:
    abs_root = os.path.abspath(workspace_root)
    
    detection = {
        "detected": False,
        "language": "Unknown",
        "framework": "Unknown",
        "package_manager": "Unknown",
        "manifest_file": "",
        "backend_dir": abs_root,
        "api_style": "REST",
        "auth_methods": [],
        "database_types": [],
        "confidence": 0.0
    }

    # Potential backend directories to check
    candidate_dirs = [
        abs_root,
        os.path.join(abs_root, "backend"),
        os.path.join(abs_root, "server"),
        os.path.join(abs_root, "api"),
        os.path.join(abs_root, "src"),
    ]

    for c_dir in candidate_dirs:
        if not os.path.exists(c_dir):
            continue

        # 1. Check Python (FastAPI, Flask, Django)
        req_txt = os.path.join(c_dir, "requirements.txt")
        pyproject = os.path.join(c_dir, "pyproject.toml")
        pipfile = os.path.join(c_dir, "Pipfile")

        if os.path.exists(req_txt) or os.path.exists(pyproject) or os.path.exists(pipfile):
            detection["language"] = "Python"
            detection["package_manager"] = "pip"
            detection["backend_dir"] = c_dir
            detection["manifest_file"] = req_txt if os.path.exists(req_txt) else (pyproject if os.path.exists(pyproject) else pipfile)
            detection["detected"] = True
            
            content = ""
            if os.path.exists(req_txt):
                with open(req_txt, "r", encoding="utf-8", errors="ignore") as f:
                    content = f.read().lower()

            if "fastapi" in content:
                detection["framework"] = "FastAPI"
                detection["confidence"] = 0.98
            elif "flask" in content:
                detection["framework"] = "Flask"
                detection["confidence"] = 0.95
            elif "django" in content:
                detection["framework"] = "Django"
                detection["confidence"] = 0.95
            else:
                detection["framework"] = "Python REST"
                detection["confidence"] = 0.80

            if "firebase" in content:
                detection["auth_methods"].append("Firebase Auth / JWT")
            if "jwt" in content or "jose" in content:
                detection["auth_methods"].append("Bearer JWT")
            if "mongo" in content or "motor" in content:
                detection["database_types"].append("MongoDB NoSQL")
            if "psycopg" in content or "asyncpg" in content or "sqlalchemy" in content:
                detection["database_types"].append("PostgreSQL / SQL")
            break

        # 2. Check Node.js / TypeScript (Express, NestJS, Koa, Fastify)
        pkg_json = os.path.join(c_dir, "package.json")
        if os.path.exists(pkg_json) and c_dir != os.path.join(abs_root, "web"):
            try:
                with open(pkg_json, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    deps = {**data.get("dependencies", {}), **data.get("devDependencies", {})}
                    if "@nestjs/core" in deps:
                        detection["language"] = "TypeScript / Node.js"
                        detection["framework"] = "NestJS"
                        detection["package_manager"] = "npm / yarn"
                        detection["manifest_file"] = pkg_json
                        detection["backend_dir"] = c_dir
                        detection["detected"] = True
                        detection["confidence"] = 0.95
                        break
                    elif "express" in deps:
                        detection["language"] = "JavaScript / Node.js"
                        detection["framework"] = "Express"
                        detection["package_manager"] = "npm / yarn"
                        detection["manifest_file"] = pkg_json
                        detection["backend_dir"] = c_dir
                        detection["detected"] = True
                        detection["confidence"] = 0.95
                        break
            except Exception:
                pass

        # 3. Check Java Spring Boot
        pom_xml = os.path.join(c_dir, "pom.xml")
        build_gradle = os.path.join(c_dir, "build.gradle")
        if os.path.exists(pom_xml) or os.path.exists(build_gradle):
            detection["language"] = "Java / Kotlin"
            detection["framework"] = "Spring Boot"
            detection["package_manager"] = "Maven" if os.path.exists(pom_xml) else "Gradle"
            detection["manifest_file"] = pom_xml if os.path.exists(pom_xml) else build_gradle
            detection["backend_dir"] = c_dir
            detection["detected"] = True
            detection["confidence"] = 0.95
            break

        # 4. Check Go (Gin, Fiber, Echo)
        go_mod = os.path.join(c_dir, "go.mod")
        if os.path.exists(go_mod):
            detection["language"] = "Go"
            detection["framework"] = "Go REST (Gin/Fiber/Echo)"
            detection["package_manager"] = "go modules"
            detection["manifest_file"] = go_mod
            detection["backend_dir"] = c_dir
            detection["detected"] = True
            detection["confidence"] = 0.95
            break

        # 5. Check Ruby on Rails
        gemfile = os.path.join(c_dir, "Gemfile")
        if os.path.exists(gemfile):
            detection["language"] = "Ruby"
            detection["framework"] = "Ruby on Rails"
            detection["package_manager"] = "Bundler"
            detection["manifest_file"] = gemfile
            detection["backend_dir"] = c_dir
            detection["detected"] = True
            detection["confidence"] = 0.95
            break

        # 6. Check PHP (Laravel / Symfony)
        composer_json = os.path.join(c_dir, "composer.json")
        if os.path.exists(composer_json):
            detection["language"] = "PHP"
            detection["framework"] = "Laravel / PHP"
            detection["package_manager"] = "Composer"
            detection["manifest_file"] = composer_json
            detection["backend_dir"] = c_dir
            detection["detected"] = True
            detection["confidence"] = 0.95
            break

        # 7. Check .NET Core / ASP.NET
        csproj_files = glob.glob(os.path.join(c_dir, "*.csproj"))
        if csproj_files:
            detection["language"] = "C# / .NET"
            detection["framework"] = "ASP.NET Core"
            detection["package_manager"] = "NuGet"
            detection["manifest_file"] = csproj_files[0]
            detection["backend_dir"] = c_dir
            detection["detected"] = True
            detection["confidence"] = 0.95
            break

    return detection

if __name__ == "__main__":
    result = detect_backend_technology(".")
    print(json.dumps(result, indent=2))
