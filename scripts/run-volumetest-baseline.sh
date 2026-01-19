#!/usr/bin/env bash
set -euo pipefail

# seed DB (always re-runs)
docker compose run --rm db-seed-baseline

# run tsung (recreate so it starts fresh)
docker compose --compatibility up --build --force-recreate tsung-volume
