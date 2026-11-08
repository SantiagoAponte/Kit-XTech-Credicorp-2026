# Prompt 02 — Reconstrucción funcional

> **Rol del agente:** Analista funcional / ingeniero de negocio (reconstruye a partir de los artefactos de Wynxx)  
> **Depende de:** Fase 01 (Aprobada)  
> **Produce:** Catálogo de reglas de negocio `BR-`, procesos, estados, cálculos, errores de negocio y comportamientos críticos, con evidencia de Wynxx

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

La fase 01 nos dio los hechos de Wynxx. Ahora hay que convertirlos en un **modelo funcional verificable**: qué hace la aplicación, con qué reglas exactas, en qué procesos (en línea y batch), con qué estados, cálculos y errores. Este catálogo es la fuente de las especificaciones OpenSpec y de las pruebas de equivalencia.

## Modo taller (dominio acotado)

**Recorte vertical.** Con el tiempo del taller no se reconstruye todo el dominio. Propón al equipo un **recorte**: de 1 a 3 procesos (≈ hasta 8 reglas `BR-`) que formen un flujo demostrable de punta a punta (pantalla → API → regla → dato → respuesta), priorizando reglas con cálculo o estado verificables (E2). Pide una confirmación breve y regístralo en `PARAMETROS.md`; lo demás va a `modernizacion/02-funcional/backlog-recorte.md`.
- Documenta en detalle completo **solo el recorte**; el resto, a nivel de inventario.
- Por cada regla del recorte, al menos 2 casos E2 (uno típico y uno límite).
- Lista las **llamadas a otros dominios** del recorte (vecino, función, datos, criticidad) para decidir qué simular.
- La «aprobación» puede ser una revisión breve en equipo; deja constancia en `aprobaciones.md`.

## Objetivo

Reconstruir el comportamiento funcional sin traducir código: reglas, validaciones, cálculos (con redondeo y precisión), procesos y flujos, estados y transiciones, restricciones, manejo de errores de negocio, integridad de datos y comportamientos críticos —incluidas las rarezas del legado que el negocio puede estar dando por buenas—, cada uno con evidencia y nivel E0–E3.

## Entradas

- Salidas de la fase 01: `modernizacion/01-analisis/**`, `modernizacion/trazabilidad/matriz-trazabilidad.*`.
- Artefactos Wynxx en `wynxx/` (reglas, procesos, entidades, hallazgos).
- Referencias a código que cite Wynxx (programas, copybooks, tablas, JCL, parámetros): se usan como evidencia, no se consultan en el COBOL.

## Instrucciones

1. **Reglas de negocio.** Para cada regla de Wynxx (y las que se deduzcan de procesos, entidades y hallazgos de Wynxx) crea una ficha `BR-<dominio>-<nnn>` con: enunciado en lenguaje de negocio; precondiciones; entradas; fórmula o lógica **exacta** (operadores, orden, tipo y precisión numérica, modo de redondeo, base de días); salidas; efectos colaterales (escrituras, llamadas, asientos); errores y códigos de retorno; **valores cableados vs. parametrizados** (dónde viven); evidencia `WXI-` (y `COB:` si Wynxx cita archivo y línea); nivel E0–E3; dominio responsable (provisional, según Wynxx).
2. **Cálculos.** Aísla cada fórmula (impuestos, intereses, cuotas, comisiones, topes, redondeos, conversiones de moneda) con sus constantes y su orden de operaciones, y anota ejemplos numéricos *derivados del código* (E2) listos para convertirse en pruebas.
3. **Procesos.** Describe cada proceso de negocio de extremo a extremo: disparador (pantalla, mensaje, job), pasos, subprocesos invocados (`CALL`), datos que lee/escribe, puntos de commit/rollback, condiciones de salida y de error. Distingue **en línea (CICS/BMS)** y **batch (JCL)**; para batch documenta la cadena de pasos, los `COND`, los archivos intermedios y la resolución dinámica de programas por parámetros.
4. **Estados y transiciones.** Solo los evidenciados (estados de cuenta, de solicitud, de lote, etc.): catálogo de estados, eventos que los cambian, guardas, y transiciones prohibidas. No completes estados «lógicos» que no estén en el código ni en Wynxx.
5. **Validaciones y errores de negocio.** Lista cada validación (campo, regla, mensaje/código) y la conducta ante fallo (rechazo, aviso, continuar, reintentar, error silencioso). Registra los casos en que el legado **falla silenciosamente** o no revierte: son comportamientos críticos.
6. **Integridad de datos.** Claves, unicidad, relaciones, invariantes entre tablas, secuencias/numeración, idempotencia y reproceso, cierres de período.
7. **Comportamientos críticos y rarezas.** Para cada caso donde el legado hace algo discutible (valores cableados, duplicación de cálculos, comparaciones con códigos inconsistentes, código muerto, límites desalineados) clasifícalo: `PRESERVAR` · `DECIDIR` (requiere decisión de negocio) · `CORREGIR-CON-APROBACIÓN`. **Nunca corrijas por tu cuenta**: crea `DESV-nnn` propuesto.
8. **Dominios provisionales.** Asigna cada regla/proceso al dominio que indica Wynxx; si hay conflicto, regístralo (no lo resuelvas aquí).
9. **Preguntas abiertas** para expertos de negocio: lista priorizada, con la regla afectada y qué decisión depende de la respuesta.
10. **Actualiza la matriz de trazabilidad**: rellena `ID-Regla` y sube niveles de evidencia.

## Uso de OpenSpec

- Todavía **no se escriben specs** en esta fase; el catálogo de reglas es la materia prima.
- Puedes usar `/opsx:explore` para investigar un comportamiento dudoso sin crear artefactos.
- Propón (solo como lista) las **capacidades candidatas** (nombres en kebab-case, agrupadas por dominio provisional) que más adelante serán `specs/<capacidad>/spec.md`. Cada capacidad debe poder describirse como contrato de comportamiento observable.
- Prepara el formato de requisito que usarás: `### Requirement:` con redacción normativa (SHALL/MUST) y `#### Scenario:` con WHEN/THEN; cada escenario será un caso de prueba (los E2 de esta fase).

## Entregables

- `modernizacion/02-funcional/reglas-negocio.md` y `.csv` (fichas `BR-`).
- `modernizacion/02-funcional/calculos.md` (fórmulas + ejemplos E2).
- `modernizacion/02-funcional/procesos.md` (en línea y batch, con diagramas Mermaid).
- `modernizacion/02-funcional/estados.md`, `validaciones-errores.md`, `integridad-datos.md`.
- `modernizacion/02-funcional/comportamientos-criticos.md` (con clasificación y `DESV` propuestos).
- `modernizacion/02-funcional/capacidades-candidatas.md`.
- `modernizacion/02-funcional/preguntas-abiertas.md`.
- Matriz de trazabilidad actualizada.

## Criterios de aceptación

- [ ] Toda regla tiene ficha completa con fórmula/lógica exacta, precisión y redondeo cuando aplica.
- [ ] Toda regla tiene evidencia `WXI-` y nivel: E1 si está corroborada en ≥2 artefactos o con código citado; E0 con vacío registrado si no.
- [ ] Todo proceso batch documenta pasos, `COND`, archivos intermedios y parámetros que cambian su comportamiento.
- [ ] Los comportamientos discutibles están clasificados y ninguno se «arregló» silenciosamente.
- [ ] Cada cálculo crítico tiene ≥2 ejemplos numéricos E2 (uno típico y uno límite).
- [ ] Las preguntas abiertas están priorizadas y ligadas a reglas.

## Trazabilidad

- `WXI-… → (COB: si Wynxx lo cita) → BR-…` completo para cada regla.
- Mapea los identificadores `RN-…` que cite Wynxx a `BR-…` (tabla `RN-legado ↔ BR`).
- Deja explícito qué reglas están en múltiples sitios del código (duplicación) con todas sus ubicaciones.

## Restricciones

- No describas la solución técnica ni el diseño futuro.
- No simplifiques reglas por parecer antiguas ni elimines casos «raros».
- No completes huecos con lógica «razonable»: regístralos.
- No ejecutes ni modifiques el legado.

## PUNTO DE APROBACIÓN

La validación de las reglas, cálculos y la clasificación `PRESERVAR/DECIDIR/CORREGIR` **requiere a un experto de negocio**. DETENTE y entrega `reglas-negocio.md`, `comportamientos-criticos.md` y `preguntas-abiertas.md` para revisión. Registra la aprobación (o las correcciones) en `modernizacion/aprobaciones.md`.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `02` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `03_ARQUITECTURA_OBJETIVO.md`
