#!/bin/bash
set -euo pipefail

# Environment variables with defaults
MONGO_DB_NAME=${MONGO_DB_NAME:-notesdb}
MONGO_COLLECTION=${MONGO_COLLECTION:-notes}
MONGO_USERNAME=${MONGO_INITDB_ROOT_USERNAME:-root}
MONGO_PASSWORD=${MONGO_INITDB_ROOT_PASSWORD:-example}

# Wait for mongod to be ready (entrypoint should handle it, but extra safety)
until mongosh --eval "db.adminCommand('ping')" >/dev/null 2>&1; do
  echo "[mongo-init] Waiting for MongoDB to be ready..."
  sleep 1
done

echo "[mongo-init] Importing data.json into ${MONGO_DB_NAME}.${MONGO_COLLECTION}"
if [ -f /docker-entrypoint-initdb.d/data.json ]; then
  mongoimport \
    --username "$MONGO_USERNAME" \
    --password "$MONGO_PASSWORD" \
    --authenticationDatabase admin \
    --db "$MONGO_DB_NAME" \
    --collection "$MONGO_COLLECTION" \
    --file /docker-entrypoint-initdb.d/data.json \
    --jsonArray \
    --drop
else
  echo "[mongo-init] data.json not found; skipping import"
fi

echo "[mongo-init] Done."
