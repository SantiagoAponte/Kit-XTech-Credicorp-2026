#!/usr/bin/env bash
# Uso: logs.sh [backend|frontend|shell|simulator|db]   (sin argumento: backend)
. "$(dirname "$0")/_lib.sh"
C="${1:-backend}"
if [ -f "$RUN_DIR/$C.log" ]; then tail -n 80 -f "$RUN_DIR/$C.log"
elif docker_ready; then ( cd "$LS_DIR" && compose --profile db --profile full --profile sim logs -f --tail=80 "$C" )
else die "no hay logs para '$C'"; fi
