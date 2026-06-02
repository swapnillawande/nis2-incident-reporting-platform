#!/bin/bash

VERSION=$1

if [ -z "$VERSION" ]; then
  echo "❌ Provide rollback version"
  exit 1
fi

export APP_VERSION=$VERSION

echo "🔁 Rolling back to $VERSION"

docker compose down
docker compose up -d

echo "✅ Rolled back to $VERSION"