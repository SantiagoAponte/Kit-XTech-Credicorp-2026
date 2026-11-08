# A4 — Formato de contratos (ADR-004)

> Obligatorio para el prompt 06. Los contratos se escriben **antes** que el código (*contract-first*) y viven en `contracts/`.

## 1. Archivos
- `contracts/<dominio>.openapi.yaml` — API del servicio del dominio (OpenAPI **3.1**).
- `contracts/neighbors/<vecino>.openapi.yaml` — lo que este dominio **consume** de otros dominios (insumo del simulador).
- `contracts/events/<dominio>.asyncapi.yaml` — solo si el dominio emite eventos (documentación).
- `contracts/error-catalog.md` — catálogo de errores con mapeo legado↔nuevo.

## 2. Reglas del API
| Tema | Regla |
|---|---|
| Versionado | Prefijo de ruta `/api/v1`; cambios rompientes ⇒ `/api/v2` |
| Recursos | Sustantivos en plural, minúsculas con guion: `/term-deposits`, `/term-deposits/{id}/cancellation` |
| Operaciones de negocio | Como sub-recurso o acción nombrada (`POST /…/cancellation`), no `PATCH` genérico con estado |
| Identificadores | Texto opaco (no exponer secuencias del legado) |
| Fechas | ISO-8601 (`2026-11-06`, `2026-11-06T14:30:00Z`) |
| Dinero | `Money = { amount: string decimal ("1234.50"), currency: "PEN"\|"USD"\|"EUR" }` |
| Tasas/porcentajes | `string` decimal con escala fija documentada |
| Paginación | `page`, `size`, `sort`; respuesta con `items`, `page`, `size`, `totalElements` |
| Idempotencia | Cabecera `Idempotency-Key` obligatoria en `POST` que crean o mueven dinero; repetir devuelve el resultado original |
| Correlación | `X-Correlation-Id` aceptado y devuelto |
| Errores | RFC 9457 `application/problem+json` con `code` estable |

## 3. Errores (RFC 9457)
```json
{
  "type": "https://errors.andes.example/term-deposit/TD-0007",
  "title": "Plazo fuera de rango",
  "status": 422,
  "detail": "El plazo 20 días es menor al mínimo de 30 días.",
  "code": "TD-0007",
  "instance": "/api/v1/term-deposits",
  "traceId": "7d1c…"
}
```
| Situación | HTTP |
|---|---|
| Formato inválido (campo faltante, tipo) | 400 |
| Regla de negocio violada | 422 |
| No existe | 404 |
| Conflicto / idempotencia / estado no permitido | 409 |
| Dependencia externa no disponible | 503 |

### Catálogo (`contracts/error-catalog.md`)
| Código nuevo | HTTP | Significado | Código legado | `BR-` | Evidencia |
|---|---|---|---|---|---|
| TD-0007 | 422 | Plazo fuera de rango | `RC=0012` | BR-DPF-003 | `WXI-reglas-014` (y `COB:` si Wynxx lo cita) |

Ningún código de retorno del legado queda sin destino. Los comportamientos que el legado resolvía en silencio y la fase 02 marcó `PRESERVAR` se documentan como respuesta explícita.

## 4. Esqueleto OpenAPI
```yaml
openapi: 3.1.0
info: { title: Term Deposits API, version: 1.0.0 }
servers: [ { url: http://localhost:8080 } ]
paths:
  /api/v1/term-deposits:
    post:
      operationId: openTermDeposit
      summary: Constituye un depósito a plazo   # BR-DPF-001..004
      parameters:
        - { name: Idempotency-Key, in: header, required: true, schema: { type: string } }
      requestBody:
        required: true
        content: { application/json: { schema: { $ref: '#/components/schemas/OpenTermDepositRequest' } } }
      responses:
        '201': { description: Creado, content: { application/json: { schema: { $ref: '#/components/schemas/TermDeposit' } } } }
        '422': { $ref: '#/components/responses/BusinessRuleViolation' }
components:
  schemas:
    Money:
      type: object
      required: [amount, currency]
      properties:
        amount: { type: string, pattern: '^-?\d+(\.\d{1,2})?$' }
        currency: { type: string, enum: [PEN, USD, EUR] }
    # … resto de esquemas
  responses:
    BusinessRuleViolation:
      description: Regla de negocio violada
      content: { application/problem+json: { schema: { $ref: '#/components/schemas/Problem' } } }
```
Cada operación y esquema relevante lleva comentario con su `BR-` y cada operación un `operationId` estable (de él se genera el cliente del frontend).

## 5. Contratos hacia vecinos (consumo)
- Un archivo por vecino con **solo las operaciones que este dominio usa**, nombradas en el lenguaje del vecino y traducidas por un adaptador anticorrupción.
- Para cada operación, define respuestas de ejemplo (éxito y cada error relevante) con `examples`: son las que servirá el simulador.

## 6. Pruebas de contrato
- **Productor (backend):** cada prueba de la capa web valida estado y cuerpo contra el esquema OpenAPI.
- **Consumidor (frontend):** el cliente tipado se **genera** desde el OpenAPI; si cambia el contrato y el frontend no compila, se detecta de inmediato.
- **Cambios rompientes:** requieren nueva versión y entrada en `modernizacion/aprobaciones.md`.

## 7. Eventos (opcional, documentación)
- AsyncAPI con nombre en pasado (`TermDepositOpened`), versión, esquema JSON, productor y consumidores conocidos. No se emiten eventos sin consumidor evidenciado.
