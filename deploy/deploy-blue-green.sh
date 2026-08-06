#!/usr/bin/env bash
set -Eeuo pipefail

: "${IMAGE:?IMAGE must be set}"
: "${TAG:?TAG must be set}"

NETWORK_NAME="todo-network"
PROXY_NAME="todo-proxy"
DEPLOY_DIR="$HOME/todo-cicd"
ACTIVE_COLOR_FILE="$DEPLOY_DIR/active-color"

mkdir -p "$DEPLOY_DIR"

if [[ -f "$ACTIVE_COLOR_FILE" ]]; then
  ACTIVE_COLOR=$(<"$ACTIVE_COLOR_FILE")
else
  ACTIVE_COLOR=""
fi

case "$ACTIVE_COLOR" in
  blue) TARGET_COLOR="green" ;;
  green) TARGET_COLOR="blue" ;;
  "") TARGET_COLOR="blue" ;;
  *) echo "Invalid active color: $ACTIVE_COLOR" >&2; exit 1 ;;
esac

TARGET_CONTAINER="todo-$TARGET_COLOR"
OLD_CONTAINER="todo-$ACTIVE_COLOR"

docker network inspect "$NETWORK_NAME" >/dev/null 2>&1 || docker network create "$NETWORK_NAME" >/dev/null
docker pull "$IMAGE:$TAG"
docker rm -f "$TARGET_CONTAINER" >/dev/null 2>&1 || true

docker run -d \
  --name "$TARGET_CONTAINER" \
  --network "$NETWORK_NAME" \
  --restart unless-stopped \
  --health-cmd='curl --fail --silent http://localhost:8080/actuator/health || exit 1' \
  --health-interval=10s \
  --health-timeout=5s \
  --health-retries=6 \
  --health-start-period=20s \
  "$IMAGE:$TAG" >/dev/null

for attempt in {1..30}; do
  HEALTH=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$TARGET_CONTAINER")
  if [[ "$HEALTH" == "healthy" ]]; then
    break
  fi
  if [[ "$HEALTH" == "unhealthy" ]]; then
    docker logs "$TARGET_CONTAINER" >&2 || true
    exit 1
  fi
  sleep 2
done

if [[ "${HEALTH:-}" != "healthy" ]]; then
  docker logs "$TARGET_CONTAINER" >&2 || true
  echo "Candidate container did not become healthy" >&2
  exit 1
fi

cat > "$DEPLOY_DIR/default.conf" <<EOF
server {
    listen 80;
    location / {
        proxy_pass http://$TARGET_CONTAINER:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

if docker container inspect "$PROXY_NAME" >/dev/null 2>&1; then
  docker exec "$PROXY_NAME" nginx -t
  docker exec "$PROXY_NAME" nginx -s reload
else
  docker run -d \
    --name "$PROXY_NAME" \
    --network "$NETWORK_NAME" \
    --restart unless-stopped \
    -p 8080:80 \
    -v "$DEPLOY_DIR/default.conf:/etc/nginx/conf.d/default.conf:ro" \
    nginx:1.27-alpine >/dev/null
fi

for attempt in {1..10}; do
  if docker exec "$PROXY_NAME" wget -q -O /dev/null http://localhost/actuator/health; then
    break
  fi
  sleep 1
done

if ! docker exec "$PROXY_NAME" wget -q -O /dev/null http://localhost/actuator/health; then
  echo "Proxy health check failed" >&2
  exit 1
fi

printf '%s' "$TARGET_COLOR" > "$ACTIVE_COLOR_FILE"
if [[ -n "$ACTIVE_COLOR" ]]; then
  docker rm -f "$OLD_CONTAINER" >/dev/null
fi

echo "Deployed $IMAGE:$TAG to $TARGET_COLOR"
