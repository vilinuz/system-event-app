#!/bin/bash
set -e

echo "🚀 Starting Full Stack Docker & Playwright End-to-End Tests"

# 1. Clean up any existing containers
echo "🧹 Cleaning up existing containers and ports..."
docker compose down -v || true
lsof -t -i :8081 | xargs -r kill -9 || true
lsof -t -i :8082 | xargs -r kill -9 || true

# 2. Run Backend Unit Tests (optional, but good practice before building container)
echo "🧪 Running backend unit tests..."
./gradlew test

# 3. Build and start the entire stack
echo "📦 Building and starting Docker containers (Postgres, Backend, Frontend)..."
docker compose up -d --build

# 4. Wait for Frontend to be Healthy
echo "⏳ Waiting for Frontend to become available on port 8082..."
MAX_RETRIES=30
RETRY_COUNT=0
HEALTH_URL="http://localhost:8082"

while ! curl -s "$HEALTH_URL" > /dev/null; do
  sleep 2
  RETRY_COUNT=$((RETRY_COUNT+1))
  if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ Frontend failed to start in time. Check docker logs."
    docker compose logs
    docker compose down
    exit 1
  fi
done

echo "✅ Frontend is up and running!"

# Give the backend an extra few seconds to seed data and fully stabilize
sleep 10

# 5. Run Playwright E2E Tests
echo "🎭 Running Playwright End-to-End Tests..."
cd frontend
npx playwright install chromium
if npx playwright test; then
    echo "✅ All Playwright tests passed!"
    TEST_RESULT=0
else
    echo "❌ Playwright tests failed!"
    TEST_RESULT=1
fi

# 6. Cleanup
cd ..
echo "🧹 Tearing down Docker stack..."
docker compose down

if [ $TEST_RESULT -eq 0 ]; then
    echo "🎉 Full Stack E2E process completed successfully!"
else
    echo "💀 E2E process failed."
    exit 1
fi
