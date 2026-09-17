FROM python:3.11-slim
WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Install resilient uvicorn interceptor to handle unexpanded $PORT from Railway UI Start Command
RUN python -c "import shutil, os; p = shutil.which('uvicorn'); open(p, 'w').write(open('uvicorn_wrapper.py').read()); os.chmod(p, 0o755)"

EXPOSE 8000
CMD ["python", "run.py"]
