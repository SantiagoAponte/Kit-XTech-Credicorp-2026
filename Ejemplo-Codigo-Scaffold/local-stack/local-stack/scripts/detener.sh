#!/usr/bin/env bash
# Detiene todo lo que levantó levantar.sh (procesos locales y contenedores). NO borra datos de la base.
. "$(dirname "$0")/_lib.sh"
for n in shell frontend backend simulator; do stop_bg "$n"; done
if docker_ready; then
  if ( cd "$LS_DIR" && compose --profile db --profile full --profile sim down --remove-orphans ) >/dev/null 2>&1; then log "contenedores detenidos"; fi
fi
log "listo"
