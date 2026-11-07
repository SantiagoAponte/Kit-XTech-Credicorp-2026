# shell — host de micro-frontends (puerto 5173)

React + TypeScript + Vite. Aporta layout con navegación, «usuario de desarrollo» (marcado **SOLO DEV**, ADR-011), tema claro/oscuro y carga perezosa de remotos por federación (`@module-federation/vite`).

## Requisitos
Node >= 20 (probado con v24.14.0) y npm.

## Comandos
| Comando | Qué hace |
|---|---|
| `npm ci` | instala del lock |
| `npm run dev` | http://localhost:5173 (proxy `/api` → `BACKEND_URL`) |
| `npm run build` | `tsc --noEmit` + build a `dist/` |
| `npm run preview` | sirve `dist` en 5173 |
| `npm test` | Vitest (navegación, usuario dev, estado de error del remoto) |
| `npm run lint` | ESLint |

Variables (ver `.env.example`; se leen al arrancar/construir):
- `VITE_REMOTE_EXAMPLE_URL` — URL del `remoteEntry.js` del remoto, **resuelta por el navegador**. Defecto `http://localhost:5174/remoteEntry.js`.
- `BACKEND_URL` — destino del proxy `/api` (defecto `http://localhost:8080`).

## Estructura
```
src/layout/    layout, usuario de desarrollo, tema
src/pages/     home, remote-module (carga perezosa + error boundary: nunca pantalla en blanco)
src/remotes/   registry.ts: lista de micro-frontends montados
src/app.tsx    rutas;  src/main.tsx arranque
```
Si el remoto no responde, `/example` muestra «El módulo «Ejemplo» no está disponible» con instrucciones; la navegación sigue operativa.

## Añadir otro micro-frontend
1. En `vite.config.ts`, añada el remoto en `federation({ remotes: { otro: { type: 'module', name: 'otro', entry: <URL> } } })` (use una variable `VITE_REMOTE_OTRO_URL`).
2. En `src/vite-env.d.ts`, declare `declare module 'otro/routes'`.
3. En `src/remotes/registry.ts`, añada `{ label, path: '/otro', load: async () => (await import('otro/routes')).routes }`.
4. El micro-frontend debe exponer `./routes` y compartir como singleton `react`, `react-dom`, `react-router-dom`.
Reglas: el shell no importa código de los micro-frontends (solo los carga en ejecución) y estos no se importan entre sí; comunicación solo por props y eventos `andes:<dominio>:<evento>`.

## Reglas de capas
El shell es solo composición: layout/rutas/carga de remotos; sin lógica de dominio. Las reglas de capas Clean Architecture aplican dentro de cada micro-frontend (ver `frontend-template/README-frontend.md`).

## Docker
`docker build --build-arg VITE_REMOTE_EXAMPLE_URL=http://localhost:5174/remoteEntry.js -t shell .` → nginx-unprivileged en 5173; `/api/` se reenvía a `BACKEND_URL` (defecto `http://backend:8080`, sustituido con envsubst al arrancar: `-e BACKEND_URL=...`). No construido en esta verificación (sin motor Docker).
