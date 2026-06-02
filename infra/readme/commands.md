# 🚀 Build System

## Build All Images

Use the build script to create versioned Docker images.

```bash
./build.sh 1.0.0

What it builds:
infra-backend:1.0.0
infra-frontend:1.0.0
infra-seeder-users:1.0.0
infra-seeder-incidents:1.0.0


🚀 Deployment System
Deploy Stack
./deploy.sh 1.0.0

This will:

Stop existing containers
Start PostgreSQL
Deploy backend
Deploy frontend
Run seeder jobs (if enabled)





Stop System
Stop all services
 -- docker compose down


 1. Build images
   ./build.sh 1.0.0

2. Deploy stack
   ./deploy.sh 1.0.0

3. Check logs
   docker logs -f nis2-backend

4. Stop system
   docker compose down