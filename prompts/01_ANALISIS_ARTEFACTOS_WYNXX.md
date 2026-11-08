# Prompt 01 — Análisis de los artefactos de Wynxx

> **Rol del agente:** Analista de modernización (lectura y clasificación; no diseña ni implementa)  
> **Depende de:** Ninguna (primera fase; requiere los Markdown de Wynxx en `wynxx/`)  
> **Produce:** Catálogo de artefactos, catálogos de hechos extraídos, registro de vacíos/contradicciones, matriz de trazabilidad v0, OpenSpec preparado

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

Estamos modernizando una aplicación bancaria COBOL/DB2/CICS/JCL/VSAM (sistema SIDEP y módulos asociados). Wynxx Legacy Transformer ya la analizó y produjo documentos Markdown. Esos documentos son la **única fuente** para reconstruir la aplicación (no se dispone del código COBOL), pero:
- no sabemos todavía cuáles están completos, cuáles se contradicen ni qué no cubren;
- en ejecuciones anteriores se observaron al menos dos tipos: un *Inventory Document* (resumen, dominios, archivos por dominio, entidades) y un *Code Analysis Document* (métricas, lenguajes, dependencias —incluyendo archivos sin referencias y dependencias faltantes—, dominios, relaciones entre dominios con «strength» y clasificación de cada archivo con confianza). **No asumas que existan todos los tipos ni que tengan esas secciones**: descúbrelo leyendo.

## Modo taller (dominio acotado)

**Alcance acotado.** En el taller cada equipo trabaja **un único dominio**. Wynxx permite exportar las reglas de negocio y los artefactos de **ese dominio**; esos son tus entradas. Antes de empezar, lee o crea `modernizacion/PARAMETROS.md` (equipo, dominio, export de Wynxx, recorte, nombre en inglés, puertos; plantilla en `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`).
- Analiza solo lo que pertenece al dominio asignado; lo demás se registra únicamente como **dependencia con vecinos**, con su dirección (entrante/saliente), para decidir luego qué simular.
- **Modo rápido:** verificación cruzada entre artefactos de al menos 5 afirmaciones por documento (prioriza reglas de cálculo y de estado); informe de síntesis breve; no generes catálogos que no vayas a usar.
- No hace falta contrastar entre dominios; sí registra contradicciones internas y vacíos.
- OpenSpec ya viene inicializado por el kit: compruébalo con `openspec list --json` y **no lo reinicialices**.

## Objetivo

Producir una visión **verificada y trazable** de todo lo que Wynxx entregó: qué documentos hay, qué hechos contienen (dominios, reglas, entidades, procesos, dependencias, hallazgos), dónde se contradicen o faltan, y una matriz de trazabilidad inicial que servirá de columna vertebral a las demás fases. Dejar OpenSpec listo para usarse.

## Entradas

- `wynxx/**/*.md` — artefactos de Wynxx (si la carpeta no existe, pide al humano que copie allí los Markdown; no los busques fuera sin autorización).
- Cualquier otro `.md` del repositorio (se clasifican como *no-Wynxx*: sirven de contexto, **no** de fuente primaria).
- Nada fuera de `wynxx/`: no se dispone del código COBOL. Si Wynxx cita fragmentos, archivos, párrafos o líneas, úsalos como evidencia.
- `openspec/` si ya existe.

## Instrucciones

1. **Inventario.** Lista todos los `.md` (ruta, tamaño, fecha, título, firma del generador, identificador de análisis, fecha de generación). Marca cada uno como `Wynxx` o `No-Wynxx` según evidencia en su cabecera; si dudas, `Indeterminado` + vacío.
2. **Clasificación por tipo de información**: inventario/archivos · métricas · dependencias · dominios y relaciones · reglas de negocio · procesos/flujos · entidades y estructuras de datos · hallazgos arquitectónicos · recomendaciones · otros. Un documento puede tener varios tipos: clasifica por sección.
3. **Extracción estructurada** a catálogos (una fila por hecho, con ID `WXI-<doc>-<nnn>` y ubicación exacta `archivo#sección`): dominios (nombre, tipo negocio/infraestructura, estado, nº de archivos, entidades, descripción, confianza), relaciones entre dominios (origen, destino, tipo, fuerza), dependencias (por archivo; archivos sin referencias; dependencias faltantes), reglas de negocio (enunciado literal, ID de origen si lo tiene), entidades/datos, procesos, hallazgos, recomendaciones.
4. **Verificación cruzada entre artefactos (obligatoria).** No se dispone del COBOL: verifica la coherencia interna de lo que entregó Wynxx. Para cada documento toma una muestra de al menos 10 afirmaciones de tipos distintos (todas si hay menos) y comprueba que se corroboran en otro artefacto o sección (p. ej. una regla citada en un proceso existe en el catálogo de reglas; las entidades de una regla existen en el modelo de datos; los archivos de un dominio aparecen en el inventario) o que el propio artefacto cita el fragmento de código que la respalda. Registra `Corroborada` / `Parcial` / `No corroborada` con la ubicación de cada corroboración. Esto eleva el hecho a `E1` o lo marca como dudoso.
5. **Detección de contradicciones** (`CON-nnn`): totales que no cuadran entre documentos o secciones; un archivo en dos dominios; relaciones asimétricas; entidades con nombres variantes que parecen el mismo concepto; reglas con valores distintos entre documentos; dominios sin archivos; archivos clasificados con confianza baja; documentos de fechas o análisis distintos mezclados.
6. **Detección de vacíos** (`GAP-nnn`): qué tipo de información NO está en ningún artefacto y sería necesaria (p. ej. reglas de cálculo, estados, errores de negocio, procesos batch, volúmenes, SLAs, datos de prueba). **Ambigüedades** (`AMB-nnn`): afirmaciones que admiten dos lecturas. Para cada uno: impacto, fase que bloquea y **cómo se resolverá antes de implementar** (quién, con qué fuente).
7. **Matriz de trazabilidad v0** (`modernizacion/trazabilidad/matriz-trazabilidad.md` y `.csv`): una fila por hecho relevante con columnas: `ID-Wynxx | Tipo | Dominio-Wynxx | Referencia-código (si Wynxx la cita) | Nivel-evidencia | Estado (Confirmado/Dudoso/Vacío) | ID-Regla (BR-…, aún vacío) | Spec-OpenSpec (aún vacío) | Componente (aún vacío) | Prueba (aún vacío)`.
8. **Preparar OpenSpec** (ver sección siguiente).
9. **Informe de síntesis** en `modernizacion/01-analisis/informe-analisis.md`: qué sabemos con confianza, qué no, y los 10 riesgos principales para la reconstrucción.

## Uso de OpenSpec

- Comprueba la instalación: `openspec --version`.
- Si no existe `openspec/` en la raíz del repositorio, **pide confirmación al humano** y ejecuta `openspec init --tools <herramienta>` (la lista de herramientas válidas sale de `openspec init --help`; para Claude Code, `claude`). Esto crea `openspec/config.yaml`, `openspec/specs/`, `openspec/changes/` y los comandos/skills de la herramienta. No crees a mano archivos de `openspec/`.
- Edita `openspec/config.yaml` (claves reales del archivo generado: `schema`, `context`, `rules`, `operations`). Mantén `schema: spec-driven` (flujo: proposal → specs → design → tasks). En `context:` pon solo restricciones que el agente no puede inferir leyendo el código: idioma de los artefactos (español), reglas de trazabilidad (IDs `BR-`, `WXI-`; `COB:` solo si Wynxx lo cita), escala de evidencia E0–E3, prohibición de cambiar comportamiento sin `DESV`, y que las specs describen comportamiento observable, no implementación. En `rules:` añade reglas por artefacto (por ejemplo: `proposal` debe citar IDs `WXI-`; `specs` debe etiquetar cada requisito con su `BR-` y evidencia; `tasks` debe incluir una tarea de prueba de equivalencia por regla).
- No crees todavía ningún *change*. Si necesitas investigar, puedes usar `/opsx:explore` (modo exploración, no genera artefactos).
- Valida que el proyecto queda sano con `openspec list --json` (debe mostrar un `root`) y `openspec schemas`.

## Entregables

- `modernizacion/01-analisis/catalogo-artefactos.md` (inventario + clasificación).
- `modernizacion/01-analisis/hechos/{dominios,relaciones,dependencias,reglas,entidades,procesos,hallazgos,recomendaciones}.md` (con IDs `WXI-…`).
- `modernizacion/01-analisis/vacios-y-contradicciones.md` (GAP / CON / AMB con impacto, bloqueo y plan de resolución).
- `modernizacion/01-analisis/verificacion-cruzada.md`.
- `modernizacion/01-analisis/informe-analisis.md`.
- `modernizacion/trazabilidad/matriz-trazabilidad.md` y `.csv` (v0).
- `modernizacion/ESTADO.md` (crearlo con la plantilla definida en `prompts/00_README.md`) y `modernizacion/aprobaciones.md` (vacío con cabecera).
- `openspec/` inicializado y `openspec/config.yaml` configurado.

## Criterios de aceptación

- [ ] Todos los `.md` del repo están inventariados y clasificados (Wynxx / No-Wynxx / Indeterminado).
- [ ] Cada hecho extraído tiene ID y ubicación exacta; no hay hechos «sueltos» sin origen.
- [ ] Cada documento Wynxx tiene verificación cruzada con resultado y ubicación de la corroboración.
- [ ] Toda contradicción, vacío y ambigüedad tiene ID, impacto, fase bloqueada y plan de resolución.
- [ ] La matriz de trazabilidad v0 existe, con todas las columnas definidas, y contiene al menos todos los dominios, relaciones y reglas encontrados.
- [ ] `openspec list --json` devuelve un `root` válido y `config.yaml` refleja el contexto y reglas pedidos.
- [ ] No se inventó ningún hecho; lo no encontrado figura como vacío.

## Trazabilidad

- Define aquí los esquemas de ID que usará toda la metodología: `WXI-<doc>-<nnn>` (hecho de Wynxx), `COB:<archivo>:<línea>` (referencia al código **citada por Wynxx**; no se lee el COBOL), `BR-<dominio>-<nnn>` (regla de negocio normalizada; se crea en la fase 02), `GAP-/CON-/AMB-/DESV-/ADR-nnn`.
- Atención: los artefactos de Wynxx pueden citar identificadores propios o comentarios `RN-XXX-NN` del código. Son **evidencia legada**, no IDs de esta metodología; regístralos como referencia y mapea a `BR-` en la fase 02.
- Cada fila de la matriz queda con su cadena incompleta (`Wynxx → BR → … `) y se irá completando en las fases siguientes.

## Restricciones

- No corrijas ni «arregles» documentos Wynxx: se leen tal cual y se registran sus defectos.
- No fusiones dominios ni propongas dominios nuevos (fase 04).
- No propongas arquitectura ni tecnología (fase 03).
- No resuelvas contradicciones por intuición: regístralas y propón cómo resolverlas.
- No uses información externa al repositorio sin marcarla como supuesto.

## PUNTO DE APROBACIÓN

Presenta al humano: (a) catálogo de artefactos, (b) lista de contradicciones/vacíos bloqueantes con su plan, (c) matriz v0 y (d) el `config.yaml` de OpenSpec. **No pases a la fase 02 sin una aprobación escrita** en `modernizacion/aprobaciones.md` (campo *Fase 01 — Aprobada por / fecha*).

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `01` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `02_RECONSTRUCCION_FUNCIONAL.md`
