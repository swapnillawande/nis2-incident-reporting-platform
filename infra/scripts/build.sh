#!/bin/bash

set -e

VERSION=$1

if [ -z "$VERSION" ]; then
  echo "❌ Please provide version"
  exit 1
fi

echo "🚀 Building NIS2 system version: $VERSION"

# ALWAYS ROOT-AGNOSTIC (works locally + GitHub CI)
ROOT="$(git rev-parse --show-toplevel)"

echo "📁 Project root detected: $ROOT"

# -------------------------
# BACKEND
# -------------------------
echo "📦 Building backend..."
docker build \
  -t nis2-backend:$VERSION \
  -f "$ROOT/Dockerfile" \
  "$ROOT"

# -------------------------
# FRONTEND
# -------------------------
echo "📦 Building frontend..."
docker build \
  -t nis2-frontend:$VERSION \
  -f "$ROOT/frontend/Dockerfile" \
  "$ROOT/frontend"

# -------------------------
# SEEDER SCRIPTS
# -------------------------
echo "📦 Building seeder scripts..."
docker build \
  -t nis2-seeder:$VERSION \
  -f "$ROOT/infra/scripts/Dockerfile" \
  "$ROOT/infra/scripts"

echo "✅ All images built successfully!"