# Prompt 07 — Implementación del backend (por incrementos, con /opsx:apply)

> **Rol del agente:** Ingeniero de software senior (implementa estrictamente lo especificado)  
> **Depende de:** Fases 05, 06 y 09 (Aprobadas); ADR de lenguaje/plataforma y de arquitectura de software de backend aceptados  
> **Produce:** Código backend del dominio piloto implementando las tareas del change, con pruebas, trazable a requisitos

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

Con specs, contratos y modelo de datos aprobados, ya se puede construir. La implementación **sigue las especificaciones**: ninguna funcionalidad se escribe si no está en un requisito. El objetivo no es traducir programas COBOL sino reconstruir el comportamiento (reglas, cálculos, estados, errores) con estructura moderna, modular y mantenible.

## Modo taller (dominio acotado)

Trabaja en el repositorio **`backend/`** copiado de `backend-template` del kit: **Java 21**, Spring Boot 3.x, Maven, **Clean Architecture** según `prompts/anexos/A2_CONVENCIONES_BACKEND_JAVA21.md`. **Parte del esqueleto del kit**: reemplaza el caso `Ping` por los casos de uso del recorte; no cambies versiones ni la estructura de capas. Las pruebas de arquitectura (ArchUnit) deben seguir pasando. **El backend debe arrancar** (perfil `dev` con PostgreSQL o perfil `nodocker` con H2) y responder a los endpoints del contrato antes de dar la fase por terminada.

## Objetivo

Implementar de forma incremental las tareas de `tasks.md` del change aprobado: dominio (agregados, invariantes, servicios de dominio), casos de uso, adaptadores y pruebas, de modo que cada requisito quede cubierto y comprobable.

## Entradas

- `openspec/changes/<change>/` aprobado (proposal, specs, design, tasks).
- `modernizacion/06-contratos/**`, `modernizacion/09-datos/**`, `modernizacion/03-arquitectura/adr/**` (aceptados).
- `modernizacion/02-funcional/calculos.md` (fórmulas exactas y ejemplos E2).
- Decisión tecnológica aceptada (lenguaje, framework, gestor de pruebas). Si no existe, DETENTE.
- `prompts/anexos/A1_ADR_COMUNES.md`, `A2_CONVENCIONES_BACKEND_JAVA21.md`, `A4_FORMATO_CONTRATOS.md` y `backend/` (copia del kit).

## Instrucciones

1. **Confirma prerrequisitos**: que existen ADR aceptados de lenguaje/plataforma, de **arquitectura de software de backend** (p. ej. Clean Architecture, con su regla de dependencia) y de estructura de proyecto; si no, propón el ADR y detente.
2. **Selecciona el change** y ejecuta `/opsx:apply <change>` (o consulta `openspec instructions apply --change <change>` y `openspec status --change <change>`). Trabaja **tarea por tarea** en el orden de `tasks.md`; al completar cada una márcala `- [x]`.
3. **Estructura del código** según `modernizacion/03-arquitectura/arquitectura-backend.md` y los ADR aceptados (capas dominio / casos de uso / adaptadores / infraestructura, puertos y adaptadores): el dominio no depende de infraestructura; contratos explícitos entre contextos; ningún acceso a datos de otro contexto salvo por su contrato. Implementa las **pruebas de arquitectura** (*fitness functions*) definidas en la fase 03 (regla de dependencia, aislamiento de datos).
4. **Dinero y números**: usa tipo decimal exacto con la escala y el modo de redondeo definidos en la fase 02 (HALF-UP, truncado, etc.); prohíbe coma flotante para importes. Reproduce el **orden de operaciones** y el redondeo de cada fórmula tal como figuran en `calculos.md`.
5. **Reglas**: cada `BR-` se implementa en un único lugar de dominio (evita reproducir duplicaciones del legado), con un comentario de trazabilidad `// BR-xxx-nnn WXI-...`. Los valores cableados del legado pasan a **configuración versionada** solo si la fase 02 lo aprobó; si no, se conservan con su valor y se documenta.
6. **Errores de negocio**: devuelve los códigos del catálogo; no repliques errores silenciosos salvo que la fase 02 los haya clasificado `PRESERVAR` (en cuyo caso se documenta la conducta explícitamente).
7. **Pruebas junto con el código**: por cada requisito/escenario, una prueba automática que lo exprese; por cada `BR-`, al menos una prueba con valores E2. Las pruebas deben poder ejecutarse sin infraestructura externa (dobles para otros contextos).
8. **Idempotencia y reproceso** donde la fase 02 lo exija (procesos batch con reinicio).
9. **Procesos batch**: impleméntalos según el ADR de batch (no replicando JCL): mismos datos de entrada/salida, mismas condiciones de parada, mismos totales de control.
10. **Levanta lo construido** cuando el incremento sea ejecutable (el procedimiento completo está en el prompt 16); no esperes al final para comprobar que arranca.
11. Tras cada grupo de tareas, ejecuta pruebas y registra resultados; si falla una, corrige el código (no la spec). Si descubres que la spec es incorrecta, **detente** y propón cambio con `/opsx:update <change>` y, si afecta comportamiento, un `DESV`.

## Uso de OpenSpec

- Flujo real: `/opsx:apply <change>` implementa las tareas pendientes y marca `- [x]` en `tasks.md`. Consulta el estado con `openspec status --change <change>` y `openspec list`.
- Si implementar revela un error o vacío en proposal/specs/design/tasks, **no lo parches en el código**: actualiza los artefactos con `/opsx:update <change>`, valida (`openspec validate <change> --strict`) y vuelve a aplicar.
- No archives el change hasta que las fases 11 y 12 validen la equivalencia (`/opsx:archive` o `openspec archive <change>`; la fusión de deltas en `openspec/specs/` puede hacerse antes con `/opsx:sync`).

## Entregables

- Código fuente del contexto en la ubicación definida por ADR (no inventes carpeta si el ADR no la fija).
- Pruebas unitarias y de componente ligadas a requisitos.
- `tasks.md` con tareas completadas.
- `modernizacion/07-backend/informe-implementacion.md`: tareas hechas, pruebas ejecutadas/resultados, decisiones menores, desvíos detectados, deuda.
- Matriz de trazabilidad: columna `Componente` rellenada (archivo/clase/módulo + prueba).

## Criterios de aceptación

- [ ] Cada tarea implementada corresponde a un requisito; no hay funcionalidad sin spec.
- [ ] Todas las pruebas del change pasan y se adjunta su salida.
- [ ] Toda `BR-` del alcance tiene comentario de traza y al menos una prueba con valor E2.
- [ ] Los importes usan decimal exacto y el redondeo documentado.
- [ ] No hay acceso a datos de otro contexto fuera de su contrato.
- [ ] Los comportamientos `DECIDIR/CORREGIR` pendientes no se implementaron.

## Trazabilidad

- `BR → Requirement → archivo:símbolo → prueba` en la matriz.
- Cada prueba cita el escenario de la spec y su origen (`E2` con cálculo, `E3` si existe dato real).

## Restricciones

- No implementes nada que no esté en `tasks.md`/specs.
- No cambies comportamiento por considerarlo «mejor»; cualquier mejora es `DESV` pendiente de aprobación.
- No generes todo el dominio de golpe: avanza por incrementos verificables (grupos de tareas).
- No elimines reglas «antiguas».
- No dispones del COBOL: no lo busques ni lo reconstruyas de memoria.
- No declares el dominio «completo» sin pasar las fases 11 y 12.

## PUNTO DE APROBACIÓN

Al cerrar cada incremento, presenta resultados de pruebas y la lista de requisitos cubiertos. No avances al siguiente incremento sin visto bueno si hubo `DESV` o hallazgos que cambien alcance.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `07` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `08_IMPLEMENTACION_FRONTEND.md (si hay pantallas en el alcance) o 10_INTEGRACION_DOMINIOS.md`
