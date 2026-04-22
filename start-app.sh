#!/bin/bash
set -e

echo "🚀 Starting Service Health & Event Monitor Application..."

# 1. Clean up any existing containers
echo "🧹 Cleaning up existing containers and ports..."
docker compose down || true
lsof -t -i :8081 | xargs -r kill -9 || true
lsof -t -i :8082 | xargs -r kill -9 || true

# 2. Build and start the entire stack
echo "📦 Building and starting Docker containers (Postgres, Backend, Frontend)..."
docker compose up -d --build

# 3. Wait for Frontend to be Healthy
echo "⏳ Waiting for Frontend to become available on port 8082..."
MAX_RETRIES=30
RETRY_COUNT=0
HEALTH_URL="http://localhost:8082"

while ! curl -s "$HEALTH_URL" > /dev/null; do
    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        echo "❌ Frontend failed to start."
        exit 1
    fi
    sleep 2
    RETRY_COUNT=$((RETRY_COUNT+1))
done

echo ""
echo "✅ Application is up and running successfully!"
echo "🌐 You can now access the frontend at: $HEALTH_URL"
echo "🔌 Backend APIs are available at: http://localhost:8081"
echo ""
echo "To stop the application, run: docker compose down"
