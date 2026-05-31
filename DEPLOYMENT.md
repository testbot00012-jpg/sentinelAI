# Sentinel AI - Production Deployment Guide

This guide provides step-by-step instructions for deploying the Sentinel AI Cybersecurity Platform across various cloud providers (VPS, Render, Railway, AWS).

## General Prerequisites
- A PostgreSQL database (Neon, Supabase, or AWS RDS).
- A Redis instance (Upstash, Render, or ElastiCache).
- Your Firebase `service_account.json` and client configurations.
- API Keys for AlienVault OTX & URLScan.io.
- Domain name for your frontend and API.

---

## 1. Deploying on a VPS (Ubuntu / Debian) using Docker Compose

If you have a Linux VPS (DigitalOcean, Linode, AWS EC2, etc.), you can run the entire stack using the provided `docker-compose.yml`.

### Step 1: Install Dependencies
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install docker.io docker-compose git -y
```

### Step 2: Clone the Repository
```bash
git clone https://github.com/your-username/sentinel-ai.git
cd sentinel-ai
```

### Step 3: Configure Environment Variables
Create a `.env` file in the root directory:
```bash
DATABASE_URL=postgresql://user:password@host:5432/sentinel_db
REDIS_URL=redis://host:6379/0
SECRET_KEY=your_secure_random_string
FIREBASE_PROJECT_ID=senthel-f8ddc
NEXT_PUBLIC_API_URL=https://api.yourdomain.com
```

### Step 4: Add Firebase Service Account
Ensure your Firebase `service_account.json` is placed in `backend/app/core/service_account.json`.

### Step 5: Start the Platform
```bash
docker-compose up -d --build
```
This will start PostgreSQL, Redis, FastAPI Backend, Next.js Frontend, and the Nginx reverse proxy.

---

## 2. Deploying on Render

Render is excellent for easy, managed deployments.

### Backend (FastAPI)
1. Go to Render Dashboard -> New -> Web Service.
2. Connect your GitHub repository.
3. Select the `backend` directory as the Root Directory.
4. Environment: `Python 3`
5. Build Command: `pip install -r requirements.txt`
6. Start Command: `uvicorn app.main:app --host 0.0.0.0 --port $PORT`
7. Add Environment Variables: `DATABASE_URL`, `REDIS_URL`, `SECRET_KEY`, etc.
8. (Note: Ensure `service_account.json` is securely loaded, or set it as a base64 environment variable and decode it at runtime).

### Frontend (Next.js)
1. Go to Render Dashboard -> New -> Web Service.
2. Connect your GitHub repository.
3. Select the `web` directory as the Root Directory.
4. Environment: `Node`
5. Build Command: `npm install && npm run build`
6. Start Command: `npm run start`
7. Add Environment Variable: `NEXT_PUBLIC_API_URL` pointing to your Render backend URL.

---

## 3. Deploying on Railway

Railway provides a highly seamless experience for full-stack apps.

### Step 1: Provision Databases
1. In your Railway project, click **New** -> **Database** -> **Add PostgreSQL**.
2. Click **New** -> **Database** -> **Add Redis**.

### Step 2: Deploy Backend
1. Click **New** -> **GitHub Repo**. Select your repository.
2. Go to Settings for this service -> Root Directory -> type `/backend`.
3. Add the environment variables from the provisioned PostgreSQL and Redis instances (e.g., `DATABASE_URL`).

### Step 3: Deploy Frontend
1. Click **New** -> **GitHub Repo**. Select your repository again.
2. Go to Settings -> Root Directory -> type `/web`.
3. Add `NEXT_PUBLIC_API_URL` pointing to the backend service's public domain.

---

## 4. Deploying on AWS (ECS + RDS + ElastiCache)

For highly scalable enterprise deployments.

### Step 1: Managed Services
- Provision an **RDS PostgreSQL** database instance.
- Provision an **ElastiCache Redis** cluster.

### Step 2: Container Registry (ECR)
Build and push your Docker images to Amazon ECR:
```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin your-account-id.dkr.ecr.us-east-1.amazonaws.com

# Backend
docker build -t sentinel-backend ./backend
docker tag sentinel-backend:latest your-account-id.dkr.ecr.us-east-1.amazonaws.com/sentinel-backend:latest
docker push your-account-id.dkr.ecr.us-east-1.amazonaws.com/sentinel-backend:latest

# Web
docker build -t sentinel-web ./web
docker tag sentinel-web:latest your-account-id.dkr.ecr.us-east-1.amazonaws.com/sentinel-web:latest
docker push your-account-id.dkr.ecr.us-east-1.amazonaws.com/sentinel-web:latest
```

### Step 3: Elastic Container Service (ECS)
1. Create a Fargate ECS Cluster.
2. Create Task Definitions for the Backend and Web services, pointing to the respective ECR images.
3. Pass environment variables via AWS Systems Manager Parameter Store or Secrets Manager.
4. Deploy the tasks behind an Application Load Balancer (ALB).

### Step 4: Domain & SSL
Use Route 53 to map your domains to the ALB, and AWS Certificate Manager (ACM) to provision free SSL certificates.

---

## Continuous Integration / Continuous Deployment (CI/CD)

We recommend using GitHub Actions for automated testing and deployment. A basic pipeline would:
1. Run `pytest` for backend unit tests.
2. Run `npm run lint` for frontend checks.
3. Build and push Docker images to your registry (Docker Hub, ECR).
4. Trigger a webhook or SSH command to update the live environment.
