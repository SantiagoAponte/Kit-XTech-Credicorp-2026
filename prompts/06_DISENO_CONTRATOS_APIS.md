# Prompt 06 — Diseño de contratos e interfaces (APIs y mensajes)

> **Rol del agente:** Arquitecto de integración / diseñador de contratos  
> **Depende de:** Fases 04 y 05 (Aprobadas)  
> **Produce:** Contratos explícitos de cada contexto: operaciones síncronas, mensajes/eventos, errores de negocio y reglas de compatibilidad, vinculados a las specs

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

En el legado, los programas se comunican mediante `CALL … USING` con áreas binarias (copybooks de interfaz) y mediante archivos, tablas compartidas y parámetros de JCL. Esos contratos son implícitos, frágiles y a veces están duplicados a mano. La modernización debe sustituirlos por **contratos explícitos, versionados y verificables**, sin perder la semántica de cada función (códigos de operación, códigos de retorno, campos obligatorios).

## Modo taller (dominio acotado)

**Formato fijo.** Los contratos siguen `prompts/anexos/A4_FORMATO_CONTRATOS.md` (OpenAPI 3.1 *contract-first*, RFC 9457, catálogo de errores, dinero como decimal, idempotencia, versionado). **No se discute el formato.** Entrega además `contracts/neighbors/<vecino>.openapi.yaml` para los vecinos del recorte (insumo del simulador, prioridad baja). Los eventos son solo documentación.

## Objetivo

Diseñar, por contexto y a partir de las specs aprobadas, los contratos de interfaz (operaciones, mensajes y eventos), el catálogo de errores de negocio, las reglas de compatibilidad y la correspondencia con las interfaces legadas, de modo que cada contrato pueda comprobarse automáticamente (contract tests).

## Entradas

- `openspec/changes/<change>/{specs,design}` aprobados (fase 05) y `modernizacion/04-dominios/**`.
- ADR aceptados sobre estilo de integración (síncrono/asíncrono) y estrategia de consistencia.
- Interfaces entre programas que describa Wynxx (llamadas, áreas de comunicación, códigos de retorno, layouts de archivos); si faltan detalles, regístralos como vacío.
- `modernizacion/01-analisis/hechos/dependencias.md`.
- `prompts/anexos/A4_FORMATO_CONTRATOS.md` (formato obligatorio) y `contracts-template/` del kit.

## Instrucciones

1. **Inventario de interfaces legadas.** Para cada dependencia entre contextos (según el mapa de la fase 04) lista: programa llamado, función/código de operación, área de comunicación (campos, tipos, longitudes, obligatoriedad), códigos de retorno y mensajes, y quién llama. Incluye también interfaces por archivo (formatos posicionales) y por tabla compartida. Señala las que están **duplicadas a mano** o desalineadas (fuente de errores).
2. **Contrato por capacidad.** Para cada capacidad con interacción externa define operación (nombre de negocio, no del programa COBOL), entrada, salida, precondiciones, errores de negocio (código estable + significado + si es reintentable), idempotencia, y consistencia esperada. Cada operación referencia el `Requirement` que la origina.
3. **Mensajes y eventos de dominio.** Para cada evento de la fase 04 define nombre, cuándo se emite, carga útil mínima, garantías (al menos una vez/orden) y consumidores. No agregues eventos sin consumidor evidenciado.
4. **Catálogo de errores de negocio.** Unifica los códigos de retorno del legado en un catálogo con mapeo **bidireccional** (`código legado ↔ código nuevo`). No pierdas ningún caso de error.
5. **Tipos comunes.** Importes (decimal exacto con moneda y escala), fechas (`AAAAMMDD` del legado ↔ formato nuevo), identificadores, códigos de producto/estado/canal. Documenta la conversión desde `COMP-3`/`PIC` y los límites de longitud que importan para la validación.
6. **Compatibilidad.** Reglas de versionado y evolución de cada contrato; qué cambios son rompientes.
7. **Formato de especificación del contrato.** Usa un formato estándar y verificable acordado en un ADR (por ejemplo, una descripción formal de APIs/mensajes). Si ese ADR aún no existe, **propón la decisión y detente** (no elijas el formato por tu cuenta).
8. **Casos de contrato** derivados de los escenarios de las specs (petición/respuesta válida, cada error, límites).

## Uso de OpenSpec

- Los **contratos externos observables** son parte del comportamiento: refleja su resumen en las `specs` del change (requisitos de entrada/salida/errores) usando `/opsx:update <change>` para mantener coherentes proposal/specs/design/tasks, y valida con `openspec validate <change> --strict`.
- Los detalles técnicos de los contratos (esquemas, versionado) van en `design.md` y en `modernizacion/06-contratos/`, no en las specs.
- Añade a `tasks.md` las tareas de contrato: generar/validar esquemas, contract tests por operación y por evento.

## Entregables

- `modernizacion/06-contratos/inventario-interfaces-legadas.md`.
- `modernizacion/06-contratos/<contexto>/contratos.md` (operaciones y eventos) y los archivos de esquema en el formato aprobado.
- `modernizacion/06-contratos/catalogo-errores.md` (mapeo bidireccional).
- `modernizacion/06-contratos/tipos-comunes.md`.
- `modernizacion/06-contratos/casos-de-contrato.md`.
- `openspec/changes/<change>` actualizado y validado.

## Criterios de aceptación

- [ ] Toda dependencia inter-contexto del legado tiene un contrato equivalente o una decisión documentada de eliminarla.
- [ ] Todo código de retorno legado está mapeado; ninguno queda sin destino.
- [ ] Cada operación y evento referencia su `Requirement` y su `BR-`.
- [ ] Los casos de contrato cubren éxito, cada error y límites.
- [ ] `openspec validate --strict` pasa tras la actualización.

## Trazabilidad

- `Wynxx (interfaz) → contrato → Requirement → caso de contrato`.
- Añade columna `Contrato` a la matriz de trazabilidad.

## Restricciones

- No copies la forma de las áreas COBOL (campos `FILLER`, redefiniciones) a las APIs nuevas; conserva la **semántica**.
- No elijas formato de especificación ni tecnología de mensajería sin ADR aprobado.
- No introduzcas operaciones o eventos sin evidencia.

## PUNTO DE APROBACIÓN

Si falta un ADR sobre formato de contratos o mensajería, **DETENTE** y pide la decisión. Los contratos de cada contexto requieren revisión del arquitecto antes de implementar el backend.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `06` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `09_PERSISTENCIA_DATOS.md (recomendado antes de 07) y luego 07_IMPLEMENTACION_BACKEND.md`
