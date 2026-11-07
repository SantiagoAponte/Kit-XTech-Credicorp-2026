#!/usr/bin/env bash
# Pruebas de humo contra lo que esté levantado. Sale con código distinto de 0 si algo falla.
. "$(dirname "$0")/_lib.sh"
FAIL=0
pass() { echo "  [PASS] $*"; }
fail() { echo "  [FAIL] $*"; FAIL=1; }
B="http://localhost:$BACKEND_PORT"
TMP="$RUN_DIR"
log "verificando (modo: $(current_mode))"

if curl -fsS "$B/actuator/health" 2>/dev/null | grep -q '"UP"'; then pass "backend salud UP"; else fail "backend salud"; fi
KEY="smoke-$(date +%s)-$$"
BODY='{"message":"smoke","amount":{"amount":"1000.00","currency":"PEN"}}'
R="$(curl -sS -o "$TMP/post.json" -w '%{http_code}' -X POST "$B/api/v1/pings" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -d "$BODY")"
if [ "$R" = 201 ]; then pass "POST /pings -> 201"; else fail "POST /pings -> $R"; fi
if grep -q '"5.00"' "$TMP/post.json"; then pass "comisión EX-R01 = 5.00"; else fail "comisión EX-R01 (esperado 5.00)"; fi
R2="$(curl -sS -o /dev/null -w '%{http_code}' -X POST "$B/api/v1/pings" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY" -d "$BODY")"
if [ "$R2" = 200 ]; then pass "repetición idempotente -> 200"; else fail "idempotencia -> $R2"; fi
R3="$(curl -sS -o "$TMP/err.json" -w '%{http_code}' -X POST "$B/api/v1/pings" -H 'Content-Type: application/json' -H "Idempotency-Key: $KEY-x" -d '{"message":"smoke","amount":{"amount":"0.00","currency":"PEN"}}')"
if [ "$R3" = 422 ] && grep -q 'EX-0003' "$TMP/err.json"; then pass "regla EX-0003 -> 422 problem+json"; else fail "EX-0003 ($R3)"; fi
if [ "$(curl -sS -o /dev/null -w '%{http_code}' "$B/api/v1/pings?page=0&size=5")" = 200 ]; then pass "GET /pings -> 200"; else fail "GET /pings"; fi
if [ "$(curl -sS -o /dev/null -w '%{http_code}' "http://localhost:$FRONTEND_PORT$REMOTE_ENTRY_PATH")" = 200 ]; then pass "micro-frontend sirve remoteEntry"; else fail "remoteEntry"; fi
if [ "$(curl -sS -o /dev/null -w '%{http_code}' "http://localhost:$SHELL_PORT/")" = 200 ]; then pass "shell sirve la página"; else fail "shell"; fi
if [ "$(curl -sS -o /dev/null -w '%{http_code}' "http://localhost:$SHELL_PORT/api/v1/pings?page=0&size=1")" = 200 ]; then pass "shell -> /api proxy hacia backend"; else fail "proxy /api del shell"; fi
if curl -fsS -o /dev/null "http://localhost:$SIMULATOR_PORT/__health" 2>/dev/null; then
  if [ "$(curl -sS -o /dev/null -w '%{http_code}' -X POST "http://localhost:$SIMULATOR_PORT/api/v1/postings")" = 201 ]; then pass "simulador de vecinos responde"; else fail "simulador"; fi
fi
echo; if [ $FAIL = 0 ]; then log "TODO OK"; else log "HAY FALLAS"; fi
exit $FAIL
