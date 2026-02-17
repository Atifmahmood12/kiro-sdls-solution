#!/bin/bash

set -e

echo "Starting Docker Compose..."
docker compose up -d --build

echo "Waiting for /health endpoint to return status ok (max 30 retries)..."

for i in {1..30}; do
  echo "Attempt $i/30..."

  # Capture response with HTTP code
  RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8080/health 2>/dev/null || true)

  # Extract HTTP code (last line)
  HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

  # Extract body (all lines except last)
  BODY=$(echo "$RESPONSE" | sed '$d')

  if [ "$HTTP_CODE" = "200" ] && echo "$BODY" | grep -q '"status":"ok"'; then
    echo "SUCCESS"
    exit 0
  fi

  sleep 1
done

echo "FAILURE"
exit 1

