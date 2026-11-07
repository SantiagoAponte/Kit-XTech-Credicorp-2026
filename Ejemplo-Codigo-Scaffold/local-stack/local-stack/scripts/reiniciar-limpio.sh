#!/usr/bin/env bash
# Detiene, BORRA el estado (volumen de PostgreSQL y logs) y vuelve a levantar en el mismo modo.
. "$(dirname "$0")/_lib.sh"
MODE="$(current_mode)"
"$LS_DIR/scripts/detener.sh"
if docker_ready; then
  ( cd "$LS_DIR" && compose --profile db --profile full --profile sim down -v --remove-orphans ) >/dev/null 2>&1
  log "volúmenes eliminados"
fi
find "${RUN_DIR:?}" -maxdepth 1 -name '*.log' -type f -delete
exec "$LS_DIR/scripts/levantar.sh" "$MODE" "$@"
