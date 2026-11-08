# T6 — Levantar y verificar (≈ 8 min) · prompt 16 reducido

> **Rol:** ingeniero de ejecución local. Meta visible: **backend Java y frontend React corriendo en localhost**, funcionando para el recorte, con evidencia real.
> **Entrada:** T5A y T5B terminados, `local-stack/` del kit. **Salida:** sistema corriendo y `modernizacion/evidencia-local.md`.

## Hazlo

1. **Detén lo que haya** de pruebas anteriores (simulador de T5B en `:8081`, servidores de desarrollo) para liberar puertos.
2. **Levanta en modo `directo`** (JDK 21 + Maven con H2, frontend y shell con Node; sin Docker) y **no preguntes el modo**: `local-stack/scripts/levantar.sh directo --sim`. Solo si falla de forma no recuperable, prueba `mixto` (PostgreSQL en contenedor) y dilo. Si un puerto está ocupado: `local-stack/scripts/detener.sh`.
3. **Adapta `local-stack/scripts/verificar.sh`.** Hoy comprueba el ejemplo `Ping` (`/api/v1/pings`, regla `EX-0003`, comisión 5.00). Reemplaza esas comprobaciones por las del dominio: salud del backend; **una operación feliz** con datos de la semilla; **un error de negocio** con su `code` esperado; idempotencia si el recorte tiene `POST` con `Idempotency-Key`; `remoteEntry` del micro-frontend; shell responde; proxy `/api` del shell llega al backend. Conserva las comprobaciones de salud, remoto y shell tal cual.
4. **Ejecuta `local-stack/scripts/verificar.sh`** y corrige hasta que termine en `TODO OK`. Ante una discrepancia entre el sistema y la spec, decide con evidencia dónde está el defecto: si es del código, corrígelo; si es de la spec, `/opsx:update <change>` y `openspec validate <change> --strict`. Nunca cambies comportamiento «para que pase».
5. **Demuestra el flujo en el navegador.** Abre `http://localhost:5173` + `RUTA_SHELL` (navegador integrado si está disponible; si no, indica la URL) y recorre: consulta con datos de la semilla → operación feliz → un error de negocio visible con su mensaje en español.
6. **Evidencia.** Escribe `modernizacion/evidencia-local.md` (una página): comandos usados, **salida real** de `verificar.sh`, URLs (`:5173` shell, `:5174` micro-frontend, `:8080` backend, `:8090` simulador), tabla `escenario del change → resultado real` (≤ 10 filas, enlazando cada una a su `Requirement`/`BR-`), y limitaciones conocidas. Nada simulado: si algo falló, di la causa.
7. Marca las tareas `[RUN]` con `Edit`. **No** ejecutes `openspec archive`.

## Problemas típicos

| Síntoma | Qué hacer |
|---|---|
| `JAVA_HOME`/JDK distinto de 21 | `export JAVA_HOME=$(/usr/libexec/java_home -v 21)` |
| Puerto ocupado | `local-stack/scripts/detener.sh`; luego `levantar.sh directo` |
| `npm ci` lento o sin red | usar `paquete-offline` / caché precargado |
| El shell no muestra el módulo | revisar el registro del remoto (T0 §4) y que `:5174/remoteEntry.js` responde |
| Datos sucios | `local-stack/scripts/reiniciar-limpio.sh` |

## Cierre

Resumen en ≤ 5 líneas: qué corre y dónde, resultado de `verificar.sh`, escenarios verificados, qué quedó en backlog y las dos frases de la demo («esto es lo que dice la spec / esto es lo que hace el sistema»). Actualiza `ESTADO.md`. **Fin del camino corto.**
