# T0 — Preparar el workspace (≈ 3 min)

> **Rol:** ingeniero de plataforma. Tarea mecánica; sin análisis. Si facilitación ya entregó el workspace preparado, verifica los puntos 1–5 y termina.

## Hazlo

1. **Parámetros.** Si `modernizacion/PARAMETROS.md` no existe o tiene campos vacíos, pregunta en **una sola pregunta** (con valores sugeridos): nombre del equipo, **dominio** (columna «Dominio recomendado» del Excel), `DOMINIO_EN` (kebab-case en inglés, p. ej. `term-deposit`), y si hay **recorte** pre-asignado por facilitación. Escribe el archivo con este formato y los puertos fijos:
   ```
   - EQUIPO: …            - DOMINIO: …            - DOMINIO_EN: …
   - EXPORT_WYNXX: wynxx/…   - RECORTE: <vacío: lo fija T1>
   - PAQUETE_BASE: com.andes.<dominio_en sin guiones>
   - REMOTO: <dominio_en sin guiones>   (nombre de federación)   - RUTA_SHELL: /<dominio_en>
   - PUERTOS: backend 8080 · shell 5173 · micro-frontend 5174 · db 5432 · simulador 8090
   - MODO_EJECUCION: directo
   ```
2. **Backend.** Si falta `backend/`, cópialo de `backend-template/`. Renombra `com.andes.example` → `PAQUETE_BASE` (carpetas, `package`, imports, `ArchitectureTest`, `pom.xml` y `spring.application.name`). No cambies versiones ni la estructura de capas.
3. **Frontend.** Si falta `frontend/`, cópialo de `frontend-template/`. Renombra el remoto federado `example` → `REMOTO` (`vite.config.ts`: `name` y `exposes`; revisa `src/remote-entry.tsx`).
4. **Shell.** Registra el remoto en `shell/`: `vite.config.ts` (`remotes`), `src/vite-env.d.ts` (`declare module '<REMOTO>/routes'`) y una entrada en `src/remotes/registry.ts` con `path: RUTA_SHELL`. Conserva la variable `VITE_REMOTE_EXAMPLE_URL` tal cual (los scripts del kit la usan).
5. **Carpetas y control.** Crea `contracts/` (si falta), `modernizacion/ESTADO.md` (tabla con filas T0…T6, estado `No iniciada`), `modernizacion/aprobaciones.md` y `modernizacion/preguntas.md` (solo cabecera). Comprueba que `openspec list --json` responde (**no** reinicialices OpenSpec).
6. **Línea base en segundo plano** (no esperes a que termine para seguir con T1): `cd backend && ./mvnw -q verify` y `cd frontend && npm ci && npm run build`. Anota en `ESTADO.md` si alguna falla.

## Criterio de salida

`backend/` y `frontend/` renombrados, remoto registrado en el shell, `PARAMETROS.md` completo, `ESTADO.md` creado. Continúa con `T1_REGLAS_Y_RECORTE.md`.
