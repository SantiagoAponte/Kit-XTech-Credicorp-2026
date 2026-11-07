#!/usr/bin/env bash
# Carga los scripts de local-stack/seeds/*.sql en PostgreSQL (modos mixto y docker).
# En modo directo la base es H2 en memoria: las semillas se cargan al arrancar mediante Flyway (backend/src/main/resources/db/seed).
. "$(dirname "$0")/_lib.sh"
MODE="$(current_mode)"
if [ "$MODE" = directo ]; then log "modo directo: las semillas las aplica Flyway al iniciar el backend (perfil nodocker)."; exit 0; fi
docker_ready || die "Docker no disponible"
shopt -s nullglob; files=("$LS_DIR"/seeds/*.sql)
if [ ${#files[@]} -eq 0 ]; then log "no hay scripts en local-stack/seeds/"; exit 0; fi
for f in "${files[@]}"; do
  log "cargando $(basename "$f")"
  ( cd "$LS_DIR" && compose exec -T db psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" ) < "$f" || die "falló $f"
done
log "datos cargados"
