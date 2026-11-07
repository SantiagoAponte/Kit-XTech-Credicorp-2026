# Versiones del frontend (fijadas el 2026-11-06)

Fuente: `npm view <paquete> version`. Ambos proyectos (`frontend-template`, `shell`) usan las mismas versiones; `package-lock.json` garantiza `npm ci` reproducible. Node v24.14.0 / npm 11.9.0.

| Paquete | Versión | Nota |
|---|---|---|
| react / react-dom | 19.3.1 | singleton en federación |
| react-router-dom | 7.18.5 | singleton en federación |
| vite | 8.3.6 | |
| @vitejs/plugin-react | 6.2.1 | peer vite ^8 |
| **typescript** | **6.0.3** | DESVIACIÓN: la última publicada es 7.0.2, pero `typescript-eslint@8.72.1` exige `<6.1.0` y `openapi-typescript` exige `^5.x`. Se usa la última 6.0.x |
| vitest | 5.1.0 | |
| jsdom | 31.0.0 | |
| @testing-library/react | 16.3.3 | |
| @testing-library/dom | 10.4.2 | peer requerido por RTL y jest-dom |
| @testing-library/jest-dom | 7.0.1 | |
| @testing-library/user-event | 14.6.8 | |
| @tanstack/react-query | 5.107.1 | solo frontend-template |
| react-hook-form | 7.91.0 | solo frontend-template |
| @hookform/resolvers | 7.0.3 | solo frontend-template |
| zod | 4.6.7 | solo frontend-template |
| openapi-typescript | 7.13.0 | generación; solo frontend-template |
| openapi-fetch | 0.17.0 | cliente; solo frontend-template |
| eslint | 10.14.0 | |
| typescript-eslint | 8.72.1 | |
| @eslint/js / globals | 10.0.1 / 17.14.0 | |
| eslint-plugin-boundaries | 7.4.2 | reglas de capas (frontend-template) |
| @module-federation/vite | 1.23.11 | federación (ambos) |
| @types/node / react / react-dom | 24.19.2 / 19.3.0 / 19.3.0 | node 24 (LTS en uso) |

## `.npmrc` con `legacy-peer-deps=true`
`openapi-typescript@7` declara peer `typescript@^5.x` y no admite TS 6 en su resolución npm, aunque funciona (verificado: `npm run generate:api`). Por eso ambos proyectos llevan `.npmrc` con `legacy-peer-deps=true`; los peers realmente necesarios (`@testing-library/dom`) se declaran explícitamente.

## Plugin de federación: `@module-federation/vite` (elegido) vs `@originjs/vite-plugin-federation`
- `@module-federation/vite@1.23.11` declara peer `vite ^5 || ^6 || ^7 || ^8` (incluye Vite 8.3.6), se publica activamente (última modificación 2026-11-06) y usa el runtime oficial de Module Federation 2.
- `@originjs/vite-plugin-federation@1.4.1` (última modificación 2026-01-04) no declara soporte para Vite 8 y está en mantenimiento mínimo.
- Verificado de verdad: build de ambos proyectos y carga real del remoto dentro del shell en un navegador (ver `VERIFICACION-FRONTEND.md`).
