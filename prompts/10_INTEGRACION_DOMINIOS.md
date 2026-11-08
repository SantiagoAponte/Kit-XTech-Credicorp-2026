# Prompt 10 — Integración entre dominios

> **Rol del agente:** Ingeniero de integración  
> **Depende de:** Fases 06, 07 y 09 de al menos dos contextos  
> **Produce:** Integraciones entre contextos implementadas y verificadas por contrato, y procesos extremo a extremo (online y batch)

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

El legado integra dominios mediante `CALL` directos, tablas compartidas y archivos. En el diseño nuevo, los contextos se integran mediante los contratos de la fase 06 (síncronos, eventos o ambos según ADR). Un proceso de negocio —por ejemplo, un desembolso que toca cliente, garantía, cartera, seguros, posteo y notificación— atraviesa varios contextos y debe conservar su resultado final y su manejo de errores.

## Modo taller (dominio acotado)

**Fase de extensión:** queda fuera del camino corto del taller (ver `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`). Ejecútala después del prompt 16, o solo si el equipo terminó con tiempo.

## Objetivo

Conectar los contextos implementados mediante sus contratos, garantizar consistencia y manejo de fallos (reintentos, compensaciones, idempotencia) y demostrar los procesos extremo a extremo con pruebas de integración y de contrato.

## Entradas

- Contextos implementados (fase 07) y contratos (fase 06), mapa de contextos (fase 04).
- `modernizacion/02-funcional/procesos.md` (flujos extremo a extremo y batch).
- ADR aceptados de consistencia, mensajería y batch.

## Instrucciones

1. **Escenarios de integración**: por cada proceso extremo a extremo (del catálogo de procesos) lista contextos participantes, orden, datos intercambiados, qué es síncrono/asíncrono y qué ocurre ante cada fallo parcial.
2. **Consistencia y compensación**: define y especifica, por proceso, el comportamiento ante fallo (qué se revierte, qué se compensa, qué se reintenta) **respetando lo que hacía el legado** (incluidos casos en que el legado *no* revierte: decide con negocio si se preserva).
3. **Idempotencia y deduplicación** en consumidores de eventos/operaciones repetibles.
4. **Integra** los contextos según los contratos (nunca por acceso directo a datos del otro). Si falta un contexto, usa un **doble** que cumpla el contrato y márcalo.
5. **Pruebas de contrato** (productor y consumidor) y **pruebas de integración** de cada proceso; incluye casos negativos y límite.
6. **Procesos batch encadenados**: reproduce la cadena (orden, condiciones de salto, archivos intermedios, totales de control) en el mecanismo aprobado, incluyendo reinicio.
7. **Ordenamiento y tiempo**: calendario de días hábiles, cortes de día, ventanas, y su efecto en los procesos.
8. **Dependencias ausentes del legado**: para cada programa llamado que no existe en el repositorio, aplica la decisión del ADR (doble documentado, especificación pendiente, etc.) sin inventar su lógica.

## Uso de OpenSpec

- Describe en las specs de un change dedicado (p. ej. `<proceso>-extremo-a-extremo`) el **comportamiento observable** del proceso con escenarios WHEN/THEN (éxito, fallo en cada paso, reintento). Crea el change con `/opsx:propose` y valida con `openspec validate --strict`.
- Implementa con `/opsx:apply`. Si cambias contratos, actualiza los artefactos con `/opsx:update` y re-valida.

## Entregables

- Código de integración, adaptadores y dobles.
- Pruebas de contrato e integración.
- `modernizacion/10-integracion/procesos-e2e.md` (diagramas de secuencia y matriz de fallos).
- Change(s) OpenSpec de procesos e2e validados.
- Matriz de trazabilidad: procesos e2e enlazados a `BR-` y pruebas.

## Criterios de aceptación

- [ ] Todo proceso e2e del alcance tiene escenarios de éxito y de cada fallo parcial relevante.
- [ ] No existe acceso directo a datos de otro contexto.
- [ ] Las pruebas de contrato pasan en ambos lados.
- [ ] La conducta ante fallo coincide con el legado o tiene `DESV` aprobado.
- [ ] Los dobles de dependencias ausentes están identificados y documentados.

## Trazabilidad

- `proceso legado (JCL/CALL chain) → proceso e2e (spec) → pruebas e2e`.
- Cada intercambio entre contextos cita su contrato y su `BR-`.

## Restricciones

- No introduzcas acoplamientos nuevos por conveniencia.
- No inventes la lógica de programas ausentes.
- No cambies la semántica de fallos sin aprobación.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `10` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `16_EJECUCION_LOCAL_VERIFICACION.md (levantar y verificar localmente) y luego 11_VALIDACION_REGLAS_NEGOCIO.md`
