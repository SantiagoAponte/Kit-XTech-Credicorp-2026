# Prompt 04 — Modelado de dominios (DDD)

> **Rol del agente:** Arquitecto de dominio (Domain-Driven Design)  
> **Depende de:** Fases 02 y 03 (Aprobadas)  
> **Produce:** Mapa de contextos acotados, lenguaje ubicuo, modelo de dominio por contexto, relaciones/contratos entre contextos y selección de dominios piloto

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

Wynxx propuso una descomposición de dominios. Esa propuesta es **hipótesis**, no mandato: hay que validarla con las reglas (fase 02), las dependencias reales y los ADR aprobados (fase 03), y refinarla con criterios DDD.

## Modo taller (dominio acotado)

**Dominio ya acotado.** No hay selección de pilotos ni mapa multi-contexto: el contexto es el dominio asignado. Produce una **versión corta**: agregados, objetos de valor, invariantes (`BR-`), eventos, puertos de entrada y de salida y los **vecinos como puertos de salida** (anticorrupción). Omite backlog de dominios y selección de pilotos. Usa el glosario es↔en del ADR-015 y el diseño de la fase 03.

## Objetivo

Definir los contextos acotados (bounded contexts) de la aplicación modernizada, su lenguaje ubicuo, agregados, entidades, objetos de valor, eventos de dominio, servicios de dominio y las relaciones entre contextos (customer/supplier, conformista, anticorrupción, núcleo compartido, etc.), y seleccionar un conjunto de **dominios piloto** para el primer incremento.

## Entradas

- `wynxx/` (dominios, relaciones y su fuerza) y `modernizacion/01-analisis/hechos/{dominios,relaciones}.md`.
- `modernizacion/02-funcional/**` y `modernizacion/03-arquitectura/**` (ADR aprobados).
- Referencias a tablas, copybooks y `CALL` compartidos que cite Wynxx, para validar fronteras.

## Instrucciones

1. **Contraste Wynxx vs. evidencia.** Para cada dominio de Wynxx comprueba cohesión (¿sus reglas hablan el mismo lenguaje?) y acoplamiento (¿qué tablas/`CALL`/copybooks comparte con otros?). Registra dominios que deban dividirse, fusionarse o reubicar archivos, siempre con evidencia. Las diferencias con Wynxx se documentan (no se «corrigen» en silencio).
2. **Lenguaje ubicuo.** Glosario por contexto (términos del negocio, siglas, códigos como productos, monedas, estados) con definición y evidencia. Detecta sinónimos y homónimos entre contextos (p. ej. un mismo término con significados distintos).
3. **Modelo por contexto.** Para cada contexto: agregados y raíces, entidades, objetos de valor (importes con moneda, tasas, períodos, identificadores), invariantes (las reglas `BR-` que protegen cada agregado), servicios de dominio, eventos de dominio (hechos de negocio que otros contextos necesitan), políticas y estados.
4. **Mapa de contextos.** Relaciones entre contextos con dirección y patrón DDD; para cada relación indica qué dato o proceso cruza y por qué. Identifica **servicios genéricos/transversales** (calendario, log, redondeo, notificaciones, parámetros) y decide, como propuesta, si son un contexto propio, una librería compartida o parte de otros contextos.
5. **Núcleo, soporte y genérico.** Clasifica subdominios (core / supporting / generic) con justificación.
6. **Fronteras transaccionales.** Qué debe cambiar atómicamente dentro de un agregado y qué puede ser consistente eventualmente entre contextos; apóyate en los ADR aceptados.
7. **Unidad de despliegue por contexto.** A partir de la propuesta de la fase 03 (servicio, módulo o librería), anota para cada contexto qué unidad lo implementa y qué datos le pertenecen; si el modelo de dominio contradice la propuesta (p. ej. dos contextos comparten un invariante que exige atomicidad), regístralo como hallazgo para revisar el ADR.
8. **Dominios piloto.** Propón 1–3 contextos para el primer incremento con criterios explícitos: valor, completitud de reglas (E1+), bajo acoplamiento entrante, poca dependencia de piezas ausentes, tamaño abordable en el taller. Incluye el *vertical slice* mínimo (de la entrada —interfaz o API— al dato persistido) y la **lista de componentes** que habría que levantar localmente para demostrarlo (insumo de la fase 16).
9. **Backlog de dominios** restantes ordenado por dependencia.

## Uso de OpenSpec

- Define la **organización de capacidades**: `openspec/specs/<contexto>/<capacidad>/spec.md` (la ruta de capacidad puede tener varios niveles, p. ej. `identity/user-auth`; mantén una organización uniforme). Documenta la convención en `modernizacion/04-dominios/convencion-capacidades.md`.
- Aún no se redactan specs: esta fase produce el mapa que las organizará.
- Actualiza `openspec/config.yaml` (`context:`) con el mapa de contextos **aprobado** (solo nombres y reglas de frontera).

## Entregables

- `modernizacion/04-dominios/mapa-de-contextos.md` (con diagrama Mermaid).
- `modernizacion/04-dominios/contexto-<nombre>.md` por contexto (modelo, invariantes, eventos).
- `modernizacion/04-dominios/lenguaje-ubicuo.md`.
- `modernizacion/04-dominios/diferencias-con-wynxx.md`.
- `modernizacion/04-dominios/dominios-piloto.md` y `backlog-dominios.md`.
- `modernizacion/04-dominios/convencion-capacidades.md`.

## Criterios de aceptación

- [ ] Cada contexto tiene reglas `BR-` asignadas y ninguna regla queda huérfana.
- [ ] Las diferencias con la descomposición de Wynxx están justificadas con evidencia.
- [ ] Cada relación entre contextos nombra el patrón y el contrato esperado.
- [ ] Los dominios piloto cumplen criterios explícitos y tienen su *vertical slice* definido.
- [ ] No se eligió ninguna tecnología.

## Trazabilidad

- Matriz: `BR- → contexto/agregado` y `WXI-dominio → contexto`.
- Cada invariante cita la `BR-` que la origina.

## Restricciones

- No diseñes tablas ni API todavía.
- No modeles conceptos que no estén en reglas, datos o procesos evidenciados.
- No dividas en servicios por defecto: el modelo de contextos es lógico; el despliegue lo definen los ADR.

## PUNTO DE APROBACIÓN

**DETENTE.** Entrega mapa de contextos, diferencias con Wynxx y dominios piloto. Espera aprobación humana y regístrala en `modernizacion/aprobaciones.md`. La elección de dominios piloto la decide el humano.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `04` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `05_ESPECIFICACIONES_OPENSPEC.md`
