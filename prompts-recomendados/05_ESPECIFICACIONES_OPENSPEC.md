# Prompt 05 — Especificaciones OpenSpec (proposal → specs → design → tasks)

> **Rol del agente:** Analista de especificaciones (Spec-Driven Development con OpenSpec)  
> **Depende de:** Fases 02, 03 y 04 (Aprobadas)  
> **Produce:** Un *change* OpenSpec por dominio piloto e incremento, con proposal, specs (deltas), design y tasks validados

## Reglas innegociables

1. **No se dispone del código COBOL; la fuente de verdad son los artefactos de Wynxx (`wynxx/`).** No busques, no reconstruyas de memoria y no modifiques código COBOL. Si falta un detalle que Wynxx no da, regístralo como vacío.
2. **No inventes.** Ni reglas de negocio, ni funcionalidades, ni tecnologías, ni comandos o estructuras de OpenSpec. Si no hay evidencia: registra un vacío `GAP-nnn` en `modernizacion/01-analisis/vacios-y-contradicciones.md`, indica cómo resolverlo y no avances sobre ese punto.
3. **Todo hecho lleva evidencia**: artefacto Wynxx (`wynxx/<archivo>#<sección>`) y, cuando ese artefacto cite el código, la referencia `COB:<archivo>:<línea o párrafo>` **tal como la cita Wynxx** (no se verifica leyendo el COBOL). Un hecho sin evidencia es un supuesto y debe etiquetarse como tal.
4. **Un cambio de comportamiento es una desviación** `DESV-nnn` en `modernizacion/aprobaciones.md`, con justificación funcional. No se implementa sin aprobación humana explícita.
5. **Las decisiones de arquitectura y tecnología son ADR** (`Proposed` → `Accepted`). Los **ADR comunes del taller** (`prompts/anexos/A1_ADR_COMUNES.md`) ya están aceptados: no se re-deciden; si la evidencia los tensiona, propón una excepción `EXC-nnn` y espera. Cuando una fase marque *PUNTO DE APROBACIÓN*, DETENTE y espera la respuesta humana; no la des por hecha.
6. **Equivalencia funcional con evidencia graduada** (nunca declares equivalencia completa sin nivel E3):
   `E0` declarado en un solo artefacto de Wynxx · `E1` corroborado: coincide en ≥2 artefactos o secciones de Wynxx, o el artefacto cita el fragmento o la ubicación del código que lo respalda · `E2` caso de prueba derivado de la regla documentada (cálculo manual o script) sin ejecutar el legado · `E3` contrastado contra ejecución real del legado o un dataset de regresión aportado.
7. **Modernizar no es traducir.** No copies la estructura de párrafos COBOL; reconstruye el comportamiento (reglas, cálculos, estados, errores de negocio) sobre una estructura moderna.
8. **OpenSpec real, no imaginado.** Antes de usar un comando, comprueba que existe con `openspec --help` / `openspec <comando> --help`.
9. **Cierra la fase** actualizando `modernizacion/ESTADO.md` (protocolo al final de este prompt).

## Verificación previa

Antes de empezar, lee `modernizacion/ESTADO.md` (si no existe y esta no es la fase 01, DETENTE: faltan fases previas). Verifica que las fases de las que depende este prompt figuren como **Aprobada**. Si alguna no lo está, no continúes: informa qué falta y por qué. Si existe `modernizacion/PARAMETROS.md` (dominio asignado, recorte, rutas, puertos), léelo y respétalo; si no existe y esta es la fase 01, créalo con el equipo (plantilla en `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`).

**Reanudación:** si ya existen entregables de esta fase (revisa las rutas de la sección *Entregables*), léelos y continúa o actualízalos; no los rehagas ni los sobrescribas sin decirlo, y anota en `ESTADO.md` qué reutilizaste.

## Contexto

OpenSpec organiza el trabajo en *changes*. Con el esquema `spec-driven`, cada change contiene `proposal.md` (qué y por qué), `specs/<capacidad>/spec.md` (deltas de comportamiento), `design.md` (cómo) y `tasks.md` (pasos con casillas `- [ ]`). Al archivar, los deltas se fusionan en `openspec/specs/`. En esta fase **solo se planifica**: no se escribe código de aplicación.

## Modo taller (dominio acotado)

Crea **un solo change** para el recorte (nombre `<dominio_en>-<recorte>` en kebab-case), con las capacidades del recorte. Los escenarios son los casos E2. `design.md` se limita a **referenciar** los ADR comunes (`anexos/A1`) y a particularizar el diseño del servicio y del micro-frontend de la fase 03; no repitas los ADR. `tasks.md` debe incluir tareas de contrato (06), datos (09), backend (07), micro-frontend (08) y ejecución local (16).

## Objetivo

Convertir las reglas, procesos y el modelo de dominio aprobados en especificaciones OpenSpec válidas, completas y trazables para los dominios piloto, con criterios de aceptación testeables y una descomposición de trabajo implementable.

## Entradas

- `modernizacion/02-funcional/**`, `modernizacion/03-arquitectura/adr/**` (aceptados), `modernizacion/04-dominios/**`.
- `modernizacion/trazabilidad/matriz-trazabilidad.*`.
- `openspec/` inicializado (fase 01).

## Instrucciones

1. **Selecciona el alcance** del primer change: un dominio piloto (o un slice) del archivo `dominios-piloto.md`. Un change por dominio/incremento; evita changes gigantes.
2. **Crea el change** con `/opsx:propose <nombre-kebab>` (genera los 4 artefactos en un paso y se detiene) o, manualmente, `openspec new change <nombre>` y luego `openspec status --change <nombre>` + `openspec instructions <artefacto> --change <nombre>` para obtener las instrucciones oficiales de cada artefacto. Respeta el orden de dependencias: proposal → specs → design → tasks.
3. **proposal.md**: por qué, qué cambia, qué queda fuera, capacidades nuevas (lista exacta, con su ruta), impacto. Cita los `WXI-`/`BR-` de origen.
4. **specs**: una `specs/<capacidad>/spec.md` por capacidad. Para capacidades nuevas la primera sección es `## Purpose` (≥50 caracteres), seguida de `## ADDED Requirements`. Cada requisito: `### Requirement: <nombre>` + descripción normativa (SHALL/MUST, ≤500 caracteres, un solo comportamiento) + ≥1 `#### Scenario: <nombre>` (exactamente 4 `#`) con `WHEN`/`THEN`. Usa los casos E2 de la fase 02 como escenarios. Incluye escenarios positivos, negativos y límite (topes, redondeos, ventanas de fechas, estados inválidos). Describe **comportamiento observable** (entradas, salidas, errores, restricciones), nunca clases ni librerías.
5. **Etiquetado de trazabilidad en cada requisito**: añade una línea corta dentro de la descripción (respetando el límite de 500 caracteres): `Trazas: BR-xxx-nnn | WXI-… | COB:archivo:línea (si Wynxx lo cita) | E1`. La traza completa vive en la matriz.
6. **design.md**: cómo se logra, citando los ADR aceptados: límites del contexto, modelo (agregados), contratos con otros contextos, estrategia de datos, manejo de errores, decisiones y alternativas, riesgos, preguntas abiertas. Aquí sí pueden aparecer decisiones técnicas **ya aprobadas** (nunca nuevas).
7. **tasks.md**: tareas pequeñas y verificables con `- [ ]`, agrupadas por incremento; cada regla `BR-` tiene su tarea de implementación **y** su tarea de prueba de equivalencia; incluye tareas de contrato, datos, integración, observabilidad y **ejecución local** (contenerización, composición, datos semilla, verificación; fase 16) cuando correspondan.
8. **Valida** con `openspec validate <nombre> --strict` y corrige hasta que pase. Revisa con `openspec show <nombre>`.
9. Si los hechos son insuficientes para un requisito, **no lo escribas**: regístralo como `GAP-` y exclúyelo del change con una nota en proposal («Fuera de alcance por vacío GAP-…»).
10. **Lista las desviaciones** (`DESV-`) que el diseño necesite y no las apliques: quedan como propuesta en `aprobaciones.md`.

## Uso de OpenSpec

- Comandos reales usados: `/opsx:propose`, `/opsx:update` (revisar artefactos manteniendo coherencia), `/opsx:explore`; CLI: `openspec new change`, `openspec status --change`, `openspec instructions`, `openspec validate [--strict]`, `openspec show`, `openspec list`. Para más flujos (`new`, `continue`, `ff`, `verify`, `bulk-archive`, `onboard`) hay que añadirlos con `openspec config profile`; compruébalo antes de usarlos.
- El flujo `propose` termina tras planificar: **no empieces a implementar** (la implementación es `/opsx:apply`, fase 07).
- No archives el change (`openspec archive`) hasta completar la implementación y validación.

## Entregables

- `openspec/changes/<change>/{proposal.md, design.md, tasks.md, specs/**/spec.md}` para el/los dominios piloto.
- Informe `modernizacion/05-specs/informe-cobertura-specs.md`: reglas `BR-` cubiertas por requisito, reglas sin spec y por qué.
- Matriz de trazabilidad actualizada (`Spec-OpenSpec` rellenada).
- Salida de `openspec validate --strict` (sin errores) guardada en el informe.

## Criterios de aceptación

- [ ] `openspec validate <change> --strict` pasa sin errores ni advertencias.
- [ ] Cada requisito tiene ≥1 escenario WHEN/THEN testeable y una línea `Trazas:`.
- [ ] Todas las `BR-` del alcance están cubiertas por un requisito o excluidas con `GAP-`/`DESV-` justificado.
- [ ] Las specs no mencionan tecnologías ni estructuras internas.
- [ ] `tasks.md` incluye tarea de implementación y de prueba de equivalencia por regla.
- [ ] El `design.md` solo usa decisiones ADR aceptadas.

## Trazabilidad

- `WXI → BR → Requirement` completo para todo requisito del change.
- Rellena en la matriz `Spec-OpenSpec` con `openspec/changes/<change>/specs/<capacidad>/spec.md#<Requirement>`.

## Restricciones

- No implementes nada.
- No escribas requisitos sin evidencia ni mezcles varios comportamientos en uno.
- No uses 3 `#` ni viñetas para escenarios (OpenSpec los ignoraría).
- No cambies comportamiento respecto al legado sin `DESV` aprobado.
- No inventes comandos OpenSpec.

## PUNTO DE APROBACIÓN

**DETENTE** antes de implementar. El humano revisa proposal, specs, design y tasks del change. Registra la aprobación en `modernizacion/aprobaciones.md` (*Change `<nombre>` — Aprobado por / fecha*). Sin esa aprobación no se ejecuta `/opsx:apply`.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `05` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `06_DISENO_CONTRATOS_APIS.md`
