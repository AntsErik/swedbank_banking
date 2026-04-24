#!/usr/bin/env bash

set -euo pipefail

port="${PORT:-8080}"
jar_path="target/banking-api-0.0.1-SNAPSHOT.jar"
external_url="${EXTERNAL_LOGGING_URL:-http://localhost:${port}/mock/external-log}"

if ! command -v java >/dev/null 2>&1; then
  echo "Java is not installed in this shell. Install Java 17+ first." >&2
  exit 1
fi

java_version_output="$(java -version 2>&1 | head -n 1)"
java_major="$(printf '%s' "$java_version_output" | sed -E 's/.*version "([0-9]+)(\..*)?".*/\1/')"

if [[ -z "$java_major" || "$java_major" -lt 17 ]]; then
  echo "Java 17+ is required in this shell. Current version: $java_version_output" >&2
  exit 1
fi

if [[ ! -f "$jar_path" ]]; then
  echo "Jar not found at $jar_path. Build it first with: mvn clean package" >&2
  exit 1
fi

exec java -jar "$jar_path" --server.port="$port" --external.logging-url="$external_url"