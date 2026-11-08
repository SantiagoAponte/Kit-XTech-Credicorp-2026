# T4 — Contrato y datos (≈ 6 min) · fusiona prompts 06 + 09 · deja el **contrato congelado**

> **Rol:** ingeniero de contratos y datos. Partes de los archivos de ejemplo del kit y los adaptas; no empieces de cero.
> **Entrada:** change aprobado, `modernizacion/02-diseno.md`, `contracts-template/` (OpenAPI de ejemplo, `error-catalog.md`, `neighbors/accounts.openapi.yaml`), `prompts/anexos/A4`.

## A. Contrato (`contracts/`)

1. `contracts/<dominio_en>.openapi.yaml` — copia `contracts-template/example.openapi.yaml` y adáptalo: OpenAPI **3.1**, prefijo `/api/v1`, un `operationId` estable por operación (de él se genera el cliente del frontend), `Money` como `{ amount: string, currency }`, `Idempotency-Key` en los `POST` que crean o mueven dinero, `X-Correlation-Id`, errores `application/problem+json`. Cada operación y esquema relevante lleva comentario con su `BR-`.
2. **Ejemplos obligatorios.** Cada respuesta (el éxito **y cada error del recorte**) lleva `example`/`examples` con datos coherentes con la semilla. El frontend desarrollará contra el simulador del kit, que sirve estos ejemplos; sin ellos T5B se bloquea.
3. `contracts/error-catalog.md` — parte del de `contracts-template/`; una fila por error del recorte: `código nuevo | HTTP | significado | código legado | BR`. Ningún error del recorte queda sin fila.
4. `contracts/neighbors/<vecino>.openapi.yaml` — **solo** los vecinos que usa el recorte, con las operaciones usadas y ejemplos de éxito y de cada error relevante (el simulador sirve el primer 2xx, o el código pedido con `?simulate=<código>`).
5. Valida con un script corto: el YAML parsea, los `$ref` resuelven, los `operationId` son únicos y cada código del catálogo aparece en el contrato. No hace falta un validador externo.

## B. Datos (`backend/src/main/resources/`)

1. `db/migration/V1__create_schema.sql` — tablas del recorte según el borrador de `02-diseno.md` (prefijo del dominio, escalas de importe de la ficha BR, claves, índices). Reemplaza `V1__create_ping.sql` del kit. Compatible con PostgreSQL y con H2 en modo PostgreSQL.
2. `db/seed/V100__seed_<dominio>.sql` — semillas **deterministas** derivadas de los casos E2 de `01-recorte.md`, con identificadores fijos y fáciles de citar en la demo (un caso típico, un límite y uno que dispare un error de negocio). Reemplaza la semilla `V100__seed_pings.sql`. Sin datos reales.

## C. Cliente del frontend

En `frontend/`: `OPENAPI_SPEC=../contracts/<dominio_en>.openapi.yaml npm run generate:api` y confirma que `npm run build` sigue compilando (si falla por el código de ejemplo `Ping`, es esperado: lo reemplaza T5B).

## CONTRATO CONGELADO

Termina con una línea en `ESTADO.md`: `contrato congelado — cambios solo editando el YAML, avisando aquí y regenerando el cliente`. Marca las tareas `[DATA]` del change con `Edit`. Las sesiones de T5A y T5B pueden empezar.

## No hagas

Inventario de interfaces legadas, casos de contrato aparte, mapeo campo a campo, estrategia de migración o conciliación, eventos AsyncAPI.

Cierre en ≤ 5 líneas y siguiente: `T5A_BACKEND.md` y `T5B_FRONTEND.md` **en paralelo**.
