# NIS2 Infra Setup

## Start PostgreSQL


```bash
docker compose -f infra/docker-compose.yml up -d
docker compose -f infra/docker-compose.yml down -v