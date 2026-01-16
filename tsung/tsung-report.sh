#!/usr/bin/env bash
set -euo pipefail

RESULTS_ROOT="${1:-./results}"
PORT="${2:-8091}"

# find tsung_stats.pl (PATH or brew)
TSUNG_STATS="$(command -v tsung_stats.pl || true)"
if [[ -z "${TSUNG_STATS}" ]] && command -v brew >/dev/null 2>&1; then
  TSUNG_STATS="$(brew list tsung 2>/dev/null | grep -E 'tsung_stats\.pl$' | head -n 1 || true)"
fi
if [[ -z "${TSUNG_STATS}" ]]; then
  echo "ERROR: tsung_stats.pl not found (brew install tsung)."
  exit 1
fi

# latest run dir
RUN_DIR="$(ls -1dt "${RESULTS_ROOT%/}"/*/ 2>/dev/null | head -n 1 || true)"
if [[ -z "${RUN_DIR}" ]]; then
  echo "ERROR: No run directories found under: $RESULTS_ROOT"
  exit 1
fi

# locate stats file (usually tsung.log)
STATS_FILE=""
if [[ -f "${RUN_DIR}tsung.log" ]]; then
  STATS_FILE="tsung.log"
else
  # fallback: pick first *.log in run dir
  STATS_FILE="$(ls -1 "${RUN_DIR}"*.log 2>/dev/null | head -n 1 || true)"
  STATS_FILE="$(basename "${STATS_FILE:-}")"
fi

if [[ -z "${STATS_FILE}" ]]; then
  echo "ERROR: No tsung.log (or any *.log) found in: $RUN_DIR"
  exit 1
fi

echo "Using latest run dir: $RUN_DIR"
echo "Generating HTML report from: $STATS_FILE"
echo "Using tsung_stats.pl: $TSUNG_STATS"

# generate report into the run directory
(
  cd "$RUN_DIR"
  perl "$TSUNG_STATS" --stats "$STATS_FILE"
)

# pick an HTML file to open
HTML_FILE=""
for f in report.html index.html; do
  [[ -f "${RUN_DIR}${f}" ]] && HTML_FILE="$f" && break
done
if [[ -z "${HTML_FILE}" ]]; then
  HTML_FILE="$(ls -1 "${RUN_DIR}"*.html 2>/dev/null | head -n 1 || true)"
  HTML_FILE="$(basename "${HTML_FILE:-}")"
fi
if [[ -z "${HTML_FILE}" ]]; then
  echo "ERROR: No HTML report generated in: $RUN_DIR"
  exit 1
fi

RUN_NAME="$(basename "${RUN_DIR%/}")"
URL="http://localhost:${PORT}/${RUN_NAME}/${HTML_FILE}"

echo "Serving: ${RESULTS_ROOT%/}"
echo "Open:    $URL"

if command -v open >/dev/null 2>&1; then
  open "$URL" || true
elif command -v xdg-open >/dev/null 2>&1; then
  xdg-open "$URL" || true
fi

exec python3 -m http.server "$PORT" --directory "${RESULTS_ROOT%/}"