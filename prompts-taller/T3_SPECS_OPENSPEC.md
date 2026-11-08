# T3 — Especificaciones OpenSpec del recorte (≈ 6 min) · prompt 05 · **única compuerta humana**

> **Rol:** analista de especificaciones. Solo planificas; no escribes código de aplicación.
> **Entrada:** `modernizacion/01-recorte.md`, `modernizacion/02-diseno.md`, `openspec/config.yaml`. **Salida:** un change validado.

## Hazlo

1. **Ajusta `openspec/config.yaml` (modo taller).** En `rules.tasks` sustituye la regla de «una tarea de prueba de equivalencia por cada BR» por: *«Una tarea por caso de uso y capa; la prueba va incluida en la tarea de implementación. Prefijos [DATA], [BE], [FE], [RUN] en cada tarea. Máximo 18 tareas.»* Si `context` menciona un dominio distinto al tuyo, corrígelo en una línea con el dominio, el paquete base y el nombre del change. No toques el resto.
2. **Crea el change** `<dominio_en>-<recorte>` (kebab-case) con `/opsx:propose` (genera proposal, specs, design y tasks en un paso). Si el comando no existe, `openspec new change <nombre>` + `openspec instructions <artefacto> --change <nombre>`.
3. **Límites de contenido:**
   - **proposal.md** ≤ 30 líneas: por qué, qué entra (UC y BR), qué queda fuera (backlog de `01-recorte.md`).
   - **specs:** 1–2 capacidades (`specs/<capacidad>/spec.md`, kebab-case). Primera sección `## Purpose` (≥ 50 caracteres), luego `## ADDED Requirements`. **Un requisito por BR**: `### Requirement: <nombre>` con SHALL/MUST (≤ 500 caracteres, un solo comportamiento) y la línea `Trazas: BR-… | wynxx/<archivo>#<sección> | E1/E2`. **≤ 2 escenarios por requisito** (`#### Scenario:` con exactamente 4 `#`, `WHEN`/`THEN`): el caso E2 típico y el límite de `01-recorte.md`. Agrega un requisito de interfaz por pantalla del recorte. Comportamiento observable, sin clases ni librerías.
   - **design.md** ≤ 40 líneas: referencia `modernizacion/02-diseno.md` y los ADR por número; no los repitas.
   - **tasks.md** ≤ 18 tareas con `- [ ]`, agrupadas así: `[DATA]` esquema y semilla · `[BE]` una por caso de uso (dominio + caso de uso + controlador + prueba con los casos E2, comentario `// BR-…`) y una de adaptadores de vecinos · `[FE]` una por pantalla (+ cliente generado, i18n de errores) · `[RUN]` levantar, adaptar `verificar.sh` y verificar el flujo de punta a punta. Los contratos (T4) van como dos tareas `[DATA]`: OpenAPI + catálogo de errores, y Flyway + semilla.
4. **Valida:** `openspec validate <change> --strict` hasta que pase. Muestra `openspec status --change <change>`.
5. Si algo falta para escribir un requisito, **no lo escribas**: anótalo en `preguntas.md` y deja la regla en el backlog.

## COMPUERTA (la única)

Muestra al equipo un resumen de **8 líneas**: change, nº de capacidades, requisitos, escenarios, tareas por prefijo, reglas en backlog y la salida de `validate`. Pregunta **una vez**: «¿Apruebas el change para implementar?» (opciones: *Aprobar* / *Ajustar*). Con *Aprobar*, escribe en `modernizacion/aprobaciones.md`: `Change <nombre> — aprobado por <nombre del equipo> / <fecha>`. Con *Ajustar*, aplica los cambios con `/opsx:update <change>`, revalida y vuelve a preguntar. **No pases a T4 sin la aprobación.**

## No hagas

Informe de cobertura, specs para reglas fuera del recorte, escenarios extra, más de 18 tareas, `openspec archive`.

Cierre en ≤ 5 líneas y siguiente: `T4_CONTRATO_Y_DATOS.md`. Actualiza `ESTADO.md`.
