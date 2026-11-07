# Verificación del frontend (2026-11-06)

Entorno: macOS arm64, Node v24.14.0, npm 11.9.0. Todas las salidas son reales (ver VERSIONES-FRONTEND.md).

## (a) npm ci / lint / test / build (partiendo de `rm -rf node_modules dist`)
| Proyecto | npm ci | lint | test | build |
|---|---|---|---|---|
| frontend-template | OK (267 paquetes) | OK, 0 errores | 15/15 pruebas (4 ficheros) | OK, `dist/remoteEntry.js` 55,41 kB |
| shell | OK (205 paquetes) | OK, 0 errores | 3/3 pruebas (1 fichero) | OK |

Total: 18 pruebas. Pruebas de frontend-template: Money (5), casos de uso con gateway falso (3), gateway HTTP con fetch simulado (3), formulario RHF+Zod con EX-0002 y diccionario de códigos (4). Shell: navegación + SOLO DEV, error de remoto no disponible, montaje de rutas del remoto.

## (b) preview + curl (5174 y 5173)
```
$ curl -sI -H "Origin: http://localhost:5173" http://localhost:5174/remoteEntry.js
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Content-Length: 55410
Content-Type: text/javascript
Last-Modified: Sat, 07 Nov 2026 04:21:15 GMT

$ curl -s http://localhost:5173/ | head -8
<!doctype html>
<html lang="es" data-theme="light">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Shell - Taller de modernización</title>
    <script type="module" crossorigin src="/assets/mf-entry-bootstrap-0-29a22be8.js"></script>
    <link rel="stylesheet" crossorigin href="/assets/index--rL8ueCT.css">

GET :5173/example (deep link) -> 200
```
`remoteEntry.js` responde 200 con `Access-Control-Allow-Origin: *`; el shell sirve el HTML (y la URL del remoto `http://localhost:5174/remoteEntry.js` quedó incrustada en su build).

## (c) Carga real del remoto dentro del shell
Se hizo en un navegador real (panel Browser del entorno, sin descargar navegadores), con `preview` de ambos y un doble HTTP en Node (puerto 18080, no el 8080 del backend) como destino del proxy `/api` del shell:
- `http://localhost:5173/example` montó la página Ping del remoto vía federación (formulario + lista con «PEN 1,234.50», comisión «PEN 6.17»).
- Enviar un mensaje de 141 caracteres → el doble respondió Problem Details `EX-0002` y la UI mostró «El mensaje no puede superar los 140 caracteres.».
- Con el remoto detenido (5174 caído) y recarga: layout y navegación visibles, «SOLO DEV», y el mensaje «El módulo «Ejemplo» no está disponible…» (sin pantalla en blanco).
- Con el backend caído (502 sin cuerpo) la lista muestra el mensaje genérico en español.
No se comprobó: contra el backend Spring real (integración pendiente), la propagación de `Idempotency-Key`/`X-Correlation-Id` end-to-end, ni otros navegadores. Las imágenes Docker NO se construyeron (sin motor); los Dockerfile/nginx no se han ejecutado ni validado con nginx.

## (d) Prueba de ruptura de capas
Se añadió en `src/presentation/pages/ping-list.tsx` el import `../../infrastructure/api/client`:
```

> frontend-template@1.0.0 lint
> eslint .


/Users/sopp/Documents/XTech - Corp/Codigo/kit/frontend-template/src/presentation/pages/ping-list.tsx
  1:33  error  There is no policy allowing dependencies from elements of type "presentation" to elements of type "infrastructure"  boundaries/dependencies

✖ 1 problem (1 error, 0 warnings)

exit_con_ruptura=1
```
Tras revertir:
```

> frontend-template@1.0.0 lint
> eslint .

exit_tras_revertir=0
```
También se comprobó (antes) que `domain → application` falla con el mismo mecanismo. Nota técnica: `eslint-plugin-boundaries@7` necesita `settings['import/resolver'].node.extensions` incluyendo `.ts/.tsx`; sin ello no resuelve imports TypeScript y la regla pasa en silencio (ya configurado en `eslint.config.js`).

## (e) Puertos
`lsof -i :5173 -i :5174` → sin salida (exit 1): ambos puertos libres. El doble Node (18080) también se detuvo.

## Limitaciones y desviaciones
- TypeScript 6.0.3 en vez de 7.0.2 (peers de typescript-eslint/openapi-typescript); `.npmrc legacy-peer-deps=true`.
- `npm audit` reporta avisos en dependencias transitivas; no se aplicó `audit fix`.
- Federación: `@module-federation/vite` (ver VERSIONES-FRONTEND.md). El HTML del shell incluye `modulepreload` del remoteEntry (hint del plugin); la carga del módulo sigue siendo perezosa y su fallo se captura.
- TanStack Query no es compartido: cada micro-frontend usa su propio QueryClient (aislamiento intencional).
