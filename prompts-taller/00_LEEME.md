# prompts-corto — camino corto del taller (≤ 50 minutos)

Versión **reducida y autocontenida** de la metodología de `prompts/` para que un equipo llegue, en una sesión de taller, a **backend Java 21 + micro-frontend React corriendo en localhost** sobre **un recorte vertical de su dominio**, construido con OpenSpec.

- Las reglas comunes, las **premisas del taller** y la política de autonomía están en [`.claude/CLAUDE.md`](../.claude/CLAUDE.md) (Claude Code lo carga solo). Los prompts de esta carpeta **no las repiten**: solo dicen qué hacer y qué entregar.
- La metodología completa (`prompts/01…16`) sigue siendo la referencia para trabajo posterior al taller.

## Ruta y tiempos

| Prompt | Fusiona | Min | Entregable principal | Compuerta |
|---|---|---|---|---|
| `T0_PREPARAR_WORKSPACE` | kit + parámetros | 3 | `backend/`, `frontend/`, `shell/` renombrados; `PARAMETROS.md` | — |
| `T1_REGLAS_Y_RECORTE` | 01 + 02 | 6 | `modernizacion/01-recorte.md` | auto |
| `T2_DISENO` | 03 + 04 | 5 | `modernizacion/02-diseno.md` | auto |
| `T3_SPECS_OPENSPEC` | 05 | 6 | change OpenSpec validado | **humano (única)** |
| `T4_CONTRATO_Y_DATOS` | 06 + 09 | 6 | OpenAPI + catálogo de errores + Flyway/semilla — **contrato congelado** | auto |
| `T5A_BACKEND` ‖ `T5B_FRONTEND` | 07 ‖ 08 | 16 | backend y micro-frontend con pruebas verdes | auto |
| `T6_LEVANTAR_Y_VERIFICAR` | 16 | 8 | sistema corriendo + `evidencia-local.md` | — |
| **Total** | | **≈ 50** | | |

`T5A` y `T5B` se ejecutan **a la vez en dos sesiones de Claude Code** (una persona del equipo en cada una) sobre el mismo workspace. Si el equipo es de una sola persona, ejecutar primero T5A y después T5B recortando alcance.

## Cómo se ejecuta

1. Abre Claude Code en la raíz del workspace.
2. Escribe: `Ejecuta @prompts-corto/T1_REGLAS_Y_RECORTE.md` (y así con cada uno, en orden).
3. No esperes a «aprobar» entre prompts: el agente continúa. Solo se detiene en T3.
4. Si un prompt se pasa de su tiempo, el agente recorta alcance (ver `CLAUDE.md`, premisa 9). Si aun así se atasca, pasa al **plan B** de abajo.

## Plan B

- Algo no arranca → `levantar.sh directo` (sin Docker, H2). El modo `mixto`/`docker` solo si el participante lo pide.
- El agente se bloquea en T5A o T5B → compara con la solución de referencia de facilitación.
- Red lenta → usar `paquete-offline` o el caché de Maven/npm precargado.

## Qué se eliminó respecto a `prompts/` (y por qué)

| Eliminado | Motivo |
|---|---|
| Catálogo de hechos `WXI-`, verificación cruzada, matriz de trazabilidad | Duplican las reglas que Wynxx ya entrega; cuestan más que lo que aportan en 50 min |
| Análisis de fronteras / diferencias con Wynxx / backlog de dominios / dominios piloto | El Excel de reasignación ya decidió el dominio |
| Copia de los 16 ADR y ficha de conformidad | La arquitectura está fijada en `anexos/A1…A5`; se referencia, no se copia |
| `DESV`, `EXC`, clasificación PRESERVAR/DECIDIR/CORREGIR | Todo se preserva; las rarezas van a `preguntas.md` |
| 8 de las 9 compuertas humanas | Solo queda la de T3 (antes de implementar) |
| Inventario de interfaces, casos de contrato, mapeo legado→nuevo, migración y conciliación | Fuera del recorte del taller |
| Informes de cobertura / implementación / pruebas, `smoke-tests.md`, `solucion-de-problemas.md` | Un solo archivo de evidencia con salida real |
| Pregunta de modo de ejecución y Docker | Por defecto `directo`; la pregunta cuesta tiempo |

## Pre-trabajo recomendado de facilitación

- Entregar el workspace con **T0 ya hecho** (carpetas copiadas y renombradas) y `~/.m2` / `node_modules` precargados (`./mvnw dependency:go-offline`, `npm ci`) mientras se da la charla de apertura.
- Preparar por dominio una **tarjeta de recorte** (3–5 `BR-` candidatas) con Wynxx; T1 la usa si existe en `PARAMETROS.md` (campo `RECORTE`).
- Verificar que el export de cada dominio coincide con el Excel (p. ej. Cheques: 14 archivos en el Excel, 8 documentos en el export).
- Ensayo cronometrado con un dominio pequeño (Fidelización, Plazo Fijo) para calibrar los tiempos.
