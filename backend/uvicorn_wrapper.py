#!/usr/bin/env python3
import sys
import os
from uvicorn.main import main

# Intercept and sanitize --port argument if passed unexpanded (e.g. '$PORT', '${PORT}', etc.)
for i, arg in enumerate(sys.argv):
    if arg == "--port" and i + 1 < len(sys.argv):
        val = sys.argv[i + 1]
        if not val.isdigit():
            sys.argv[i + 1] = os.environ.get("PORT", "8000")

if __name__ == "__main__":
    sys.exit(main())
