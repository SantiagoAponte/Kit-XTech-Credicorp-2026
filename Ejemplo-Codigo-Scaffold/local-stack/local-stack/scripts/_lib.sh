#!/usr/bin/env bash
# Utilidades comunes de los scripts del kit (macOS / Linux; en Windows usar WSL o Git Bash).
set -u
LS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ROOT="$(cd "$LS_DIR/.." && pwd)"
RUN_DIR="$LS_DIR/.run"; mkdir -p "$RUN_DIR"
BACKEND_DIR="$ROOT/backend"; FRONTEND_DIR="$ROOT/frontend"; SHELL_DIR="$ROOT/shell"
SIM_DIR="$LS_DIR/simulator"

if [ -f "$LS_DIR/.env" ]; then set -a; . "$LS_DIR/.env"; set +a; fi
: "${POSTGRES_DB:=kitdb}"; : "${POSTGRES_USER:=kit}"; : "${POSTGRES_PASSWORD:=kit}"
: "${DB_PORT:=5432}"; : "${BACKEND_PORT:=8080}"; : "${SHELL_PORT:=5173}"
: "${FRONTEND_PORT:=5174}"; : "${SIMULATOR_PORT:=8090}"
: "${REMOTE_ENTRY_PATH:=/remoteEntry.js}"

log() { printf '[kit] %s\n' "$*"; }
die() { printf '[kit] ERROR: %s\n' "$*" >&2; exit 1; }
have() { command -v "$1" >/dev/null 2>&1; }

compose() {
  # Acepta el plugin "docker compose" o el binario independiente "docker-compose" (habitual con Colima).
  if docker compose version >/dev/null 2>&1; then
    docker compose -f "$LS_DIR/compose.yaml" --project-directory "$LS_DIR" "$@"
  elif have docker-compose; then
    docker-compose -f "$LS_DIR/compose.yaml" --project-directory "$LS_DIR" "$@"
  else
    die "No se encontró Docker Compose ('docker compose' ni 'docker-compose')."
  fi
}

set_java() {
  # Maven y Spring Boot de este kit requieren JDK 21.
  if [ -n "${JAVA_HOME:-}" ] && "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"21'; then return 0; fi
  if [ -x /usr/libexec/java_home ]; then
    local jh; jh="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
    if [ -n "$jh" ]; then export JAVA_HOME="$jh"; return 0; fi
  fi
  if have java && java -version 2>&1 | grep -q '"21'; then return 0; fi
  die "No se encontró un JDK 21. Instálalo o define JAVA_HOME."
}

mvn_cmd() { if [ -x "$BACKEND_DIR/mvnw" ]; then echo "./mvnw"; else echo "mvn"; fi; }

start_bg() { # start_bg <nombre> <directorio> <comando...>
  local name="$1" dir="$2"; shift 2
  stop_bg "$name" quiet
  # El proceso se lanza como comando simple (no lista AND) con stdio redirigido: así $! es su pid real
  # y no hereda la salida del script (evita bloquear a quien capture la salida con una tubería).
  ( set -m; cd "$dir" || exit 1; "$@" > "$RUN_DIR/$name.log" 2>&1 < /dev/null & echo $! > "$RUN_DIR/$name.pid" )
  log "iniciado $name (pid $(cat "$RUN_DIR/$name.pid")) · log: local-stack/.run/$name.log"
}

stop_bg() { # stop_bg <nombre> [quiet]
  local name="${1:?nombre requerido}" pf
  pf="${RUN_DIR:?}/$name.pid"
  [ -f "$pf" ] || return 0
  local pid; pid="$(cat "$pf")"
  if kill -0 "$pid" 2>/dev/null; then
    kill -TERM -- "-$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null || true
    for _ in 1 2 3 4 5 6 7 8 9 10; do kill -0 "$pid" 2>/dev/null || break; sleep 1; done
    if kill -0 "$pid" 2>/dev/null; then kill -KILL -- "-$pid" 2>/dev/null || kill -KILL "$pid" 2>/dev/null || true; fi
    [ "${2:-}" = quiet ] || log "detenido $name"
  fi
  rm -f "$pf"
}

wait_http() { # wait_http <url> <segundos> <nombre>
  local url="$1" secs="${2:-90}" name="${3:-$1}" i=0
  while [ "$i" -lt "$secs" ]; do
    if curl -fsS -o /dev/null "$url" 2>/dev/null; then log "listo: $name ($url)"; return 0; fi
    sleep 1; i=$((i+1))
  done
  log "TIEMPO AGOTADO esperando $name ($url)"; return 1
}

port_busy() { lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1; }

current_mode() {
  local m=""
  [ -f "$RUN_DIR/modo" ] && m="$(cat "$RUN_DIR/modo")"
  if [ -z "$m" ] && [ -f "$ROOT/modernizacion/PARAMETROS.md" ]; then
    m="$(grep -E '^- MODO_EJECUCION:' "$ROOT/modernizacion/PARAMETROS.md" | sed -E 's/^- MODO_EJECUCION:[[:space:]]*//' | awk '{print $1}')"
  fi
  case "$m" in directo|mixto|docker) echo "$m" ;; *) echo "directo" ;; esac
}

docker_ready() { have docker && docker info >/dev/null 2>&1; }
