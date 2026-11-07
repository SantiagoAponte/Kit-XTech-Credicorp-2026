#!/usr/bin/env bash
# Uso: levantar.sh [directo|mixto|docker] [--sim]
#   directo : backend (JDK 21 + Maven, perfil nodocker/H2) y frontends con Node. Sin Docker.
#   mixto   : solo PostgreSQL en contenedor; backend (perfil dev) y frontends directos.
#   docker  : todo en contenedores (perfil full de compose.yaml).
#   --sim   : levanta además el simulador de dominios vecinos (prioridad baja).
. "$(dirname "$0")/_lib.sh"

MODE=""; SIM=0
for a in "$@"; do
  case "$a" in
    directo|mixto|docker) MODE="$a" ;;
    --sim) SIM=1 ;;
    -h|--help) sed -n '2,7p' "$0"; exit 0 ;;
    *) die "argumento desconocido: $a" ;;
  esac
done
[ -z "$MODE" ] && MODE="$(current_mode)"
echo "$MODE" > "$RUN_DIR/modo"
log "modo de ejecución: $MODE"

for d in "$BACKEND_DIR" "$FRONTEND_DIR" "$SHELL_DIR"; do [ -d "$d" ] || die "falta la carpeta $d (¿corriste crear-workspace.sh?)"; done

ports="$BACKEND_PORT $SHELL_PORT $FRONTEND_PORT"
[ "$MODE" != directo ] && ports="$ports $DB_PORT"
[ "$MODE" = docker ] || [ "$SIM" = 0 ] || ports="$ports $SIMULATOR_PORT"
for p in $ports; do
  if port_busy "$p"; then die "el puerto $p ya está en uso (usa ./detener.sh o cambia el puerto en local-stack/.env)"; fi
done

ensure_node_deps() { # <dir>
  if [ ! -d "$1/node_modules" ]; then
    log "instalando dependencias en $1 (npm ci)"
    ( cd "$1" && npm ci --no-audit --no-fund >/dev/null ) || die "falló npm ci en $1"
  fi
}

start_node_apps() {
  have node && have npm || die "se requiere Node.js y npm"
  ensure_node_deps "$FRONTEND_DIR"; ensure_node_deps "$SHELL_DIR"
  start_bg frontend "$FRONTEND_DIR" env BACKEND_URL="http://localhost:$BACKEND_PORT" npm run dev
  start_bg shell "$SHELL_DIR" env BACKEND_URL="http://localhost:$BACKEND_PORT" VITE_REMOTE_EXAMPLE_URL="http://localhost:$FRONTEND_PORT$REMOTE_ENTRY_PATH" npm run dev
}

start_simulator_direct() {
  have node || die "se requiere Node.js para el simulador"
  ensure_node_deps "$SIM_DIR"
  start_bg simulator "$SIM_DIR" env PORT="$SIMULATOR_PORT" CONTRACTS_DIR="$ROOT/contracts/neighbors" node server.mjs
}

case "$MODE" in
  directo)
    set_java
    start_bg backend "$BACKEND_DIR" env SPRING_PROFILES_ACTIVE=nodocker "$(mvn_cmd)" -q spring-boot:run
    start_node_apps
    [ "$SIM" = 1 ] && start_simulator_direct
    ;;
  mixto)
    docker_ready || die "Docker no está disponible (¿motor detenido?). Usa el modo 'directo' o inicia Docker."
    set_java
    ( cd "$LS_DIR" && compose --profile db up -d --wait db ) || die "no se pudo levantar la base de datos"
    start_bg backend "$BACKEND_DIR" env SPRING_PROFILES_ACTIVE=dev \
      DB_URL="jdbc:postgresql://localhost:$DB_PORT/$POSTGRES_DB" DB_USER="$POSTGRES_USER" DB_PASSWORD="$POSTGRES_PASSWORD" \
      "$(mvn_cmd)" -q spring-boot:run
    start_node_apps
    [ "$SIM" = 1 ] && start_simulator_direct
    ;;
  docker)
    docker_ready || die "Docker no está disponible (¿motor detenido?). Usa el modo 'directo'."
    profiles="--profile full"; [ "$SIM" = 1 ] && profiles="$profiles --profile sim"
    ( cd "$LS_DIR" && compose $profiles up -d --build --wait ) || die "falló la composición (revisa: ./logs.sh)"
    ;;
esac

ok=0
wait_http "http://localhost:$BACKEND_PORT/actuator/health" 150 backend || ok=1
wait_http "http://localhost:$FRONTEND_PORT$REMOTE_ENTRY_PATH" 90 "micro-frontend (remoteEntry)" || ok=1
wait_http "http://localhost:$SHELL_PORT/" 90 shell || ok=1
if [ "$SIM" = 1 ]; then wait_http "http://localhost:$SIMULATOR_PORT/__health" 30 simulador || ok=1; fi

echo
log "URLs:  shell http://localhost:$SHELL_PORT  ·  micro-frontend http://localhost:$FRONTEND_PORT  ·  backend http://localhost:$BACKEND_PORT/actuator/health"
log "Siguiente paso: ./verificar.sh   (detener: ./detener.sh)"
exit $ok
