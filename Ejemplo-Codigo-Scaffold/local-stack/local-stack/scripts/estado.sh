#!/usr/bin/env bash
# Muestra el estado de procesos, puertos y salud.
. "$(dirname "$0")/_lib.sh"
log "modo actual: $(current_mode)"
for n in backend frontend shell simulator; do
  pf="$RUN_DIR/$n.pid"
  if [ -f "$pf" ] && kill -0 "$(cat "$pf")" 2>/dev/null; then echo "  proceso $n: activo (pid $(cat "$pf"))"; else echo "  proceso $n: no activo"; fi
done
chk() { if curl -fsS -o /dev/null "$2" 2>/dev/null; then echo "  $1: OK  ($2)"; else echo "  $1: sin respuesta ($2)"; fi; }
chk backend "http://localhost:$BACKEND_PORT/actuator/health"
chk micro-frontend "http://localhost:$FRONTEND_PORT$REMOTE_ENTRY_PATH"
chk shell "http://localhost:$SHELL_PORT/"
chk simulador "http://localhost:$SIMULATOR_PORT/__health"
if docker_ready; then
  echo "  contenedores:"
  ( cd "$LS_DIR" && compose --profile db --profile full --profile sim ps --format 'table {{.Name}}\t{{.Status}}' 2>/dev/null | sed 's/^/    /' )
fi
