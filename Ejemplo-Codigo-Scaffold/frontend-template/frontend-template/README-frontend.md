# frontend-template — micro-frontend remoto (puerto 5174)

React + TypeScript estricto + Vite, organizado con Clean Architecture (A3) y expuesto por federación de módulos (`@module-federation/vite`) para que el **shell** lo monte bajo `/example`.

## Requisitos
Node >= 20 (probado con v24.14.0) y npm. No requiere el backend para tests ni build.

## Comandos
| Comando | Qué hace |
|---|---|
| `npm ci` | instala dependencias exactas del lock |
| `npm run dev` | modo independiente en http://localhost:5174 (proxy `/api` → `BACKEND_URL`, por defecto `http://localhost:8080`) |
| `npm run build` | `tsc --noEmit` + build a `dist/` (incluye `dist/remoteEntry.js`) |
| `npm run preview` | sirve `dist` en 5174 con CORS abierto y proxy `/api` |
| `npm test` | Vitest + Testing Library |
| `npm run lint` | ESLint + reglas de capas (`eslint-plugin-boundaries`) |
| `npm run generate:api` | regenera `src/infrastructure/api/schema.d.ts` desde el OpenAPI; ruta con `OPENAPI_SPEC=ruta.yaml` (defecto `../contracts-template/example.openapi.yaml`). El código generado se commitea |

Variables: `BACKEND_URL` (proxy dev/preview), `OPENAPI_SPEC` (ver `.env.example`).

## Estructura
```
src/domain/          Money (importe string, sin number), Ping
src/application/     casos de uso createPing/listPings/getPing, puerto PingGateway, ApplicationError
src/infrastructure/  api/ (cliente openapi-fetch + schema generado), gateways/, mappers/
src/presentation/    páginas (formulario RHF+Zod, lista paginada TanStack Query), hooks, i18n (code → mensaje), routes
src/bootstrap/       composition-root.ts: único sitio que ensambla adaptadores y casos de uso
src/remote-entry.tsx expone ./routes   ·  src/app.tsx expone ./App  ·  src/main.tsx modo independiente
```

## Reglas de capas (verificadas por lint)
- `domain` no importa de ninguna otra capa; `application` solo de `domain`.
- `infrastructure` importa de `application`/`domain` (implementa puertos).
- `presentation` importa solo de `application`/`domain`; **nunca** de `infrastructure`, ni usa `fetch` (regla `no-restricted-globals`).
- Solo `bootstrap/` y los ficheros de `src/*.tsx` (composition root y puntos de entrada) pueden tocar todas las capas.
- Ningún import entre micro-frontends ni hacia el shell (`no-restricted-imports`).
- Los importes son siempre `{ amount: "string", currency }`; no convertir a `number`.

## Federación
Nombre `example`, `filename: remoteEntry.js`, expone `./routes` (rutas relativas, el shell las monta en `/example`) y `./App`. Compartidos singleton: `react`, `react-dom`, `react-router-dom`. Estilos con CSS Modules (aislados).

## Reemplazar el ejemplo por un dominio
1. Sustituya el contrato (`contracts/<dominio>.openapi.yaml`) y ejecute `OPENAPI_SPEC=... npm run generate:api`.
2. Reemplace `domain/ping.ts`, el puerto `PingGateway`, los casos de uso, el gateway y los mapeadores por los de su dominio (mantenga `Money`).
3. Reemplace las páginas de `presentation/` y el diccionario `i18n/error-messages.ts` con los códigos de su catálogo.
4. Ajuste `composition-root.ts`, y en `vite.config.ts` el `name` de la federación (y la ruta/`path` del remoto en el shell: `shell/src/remotes/registry.ts`).
5. Actualice cada prueba citando su `BR-`/escenario.

## Docker
`docker build -t frontend-template .` → nginx-unprivileged en 5174 con `Access-Control-Allow-Origin: *`. (No construido en esta verificación: sin motor Docker.)
