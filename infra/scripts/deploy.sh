#!/bin/bash

VERSION=$1

if [ -z "$VERSION" ]; then
  echo "❌ Please provide version"
  exit 1
fi

export APP_VERSION=$VERSION

echo "🚀 Deploying version $APP_VERSION"

docker compose down

docker compose up -d

echo "✅ Deployed $APP_VERSION"