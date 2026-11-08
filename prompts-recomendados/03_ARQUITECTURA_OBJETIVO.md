# Prompt 03 — Arquitectura objetivo PREDEFINIDA (aplicar, no re-decidir)

> **Rol del agente:** Arquitecto de software que **aplica** la arquitectura común del taller al dominio asignado (no la redefine)  
> **Depende de:** Fases 01 y 02 (Aprobadas)  
> **Produce:** ADRs comunes instalados en el espacio de trabajo, ficha de conformidad, diseño del servicio y del micro-frontend del dominio, topología local, reglas de arquitectura verificables y excepciones propuestas (si las hay)

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

En este taller **todos los equipos construyen sobre la misma arquitectura**, ya decidida por facilitación y recogida en los anexos de esta carpeta:

- **Sistema:** microservicios; el dominio asignado es **un servicio** con su base de datos y su micro-frontend.
- **Backend:** **Java 21** + Spring Boot 3.x + Maven, con **Clean Architecture** (`domain` ← `application` ← `adapter` ← `infrastructure`).
- **Frontend:** **React + TypeScript + Vite**, como **micro-frontend remoto** compuesto por un shell, con **Clean Architecture** (`domain` / `application` / `infrastructure` / `presentation`).
- **Contratos:** OpenAPI 3.1 *contract-first*, errores RFC 9457, catálogo de errores, dinero como decimal.
- **Datos:** base de datos por servicio; PostgreSQL en contenedor (H2 sin Docker); migraciones Flyway.
- **Ejecución local:** Docker (una imagen por unidad) con alternativa sin Docker.

La fuente de verdad son `prompts/anexos/A1_ADR_COMUNES.md` (16 ADR, estado *Accepted — predefinido por facilitación*), `A2` (backend), `A3` (frontend), `A4` (contratos) y `A5` (kit y ruta). **Esta fase no consiste en elegir arquitectura, sino en aplicarla con rigor al dominio y en detectar dónde la evidencia del legado la tensiona.**

## Objetivo

Instalar los ADR comunes en el espacio de trabajo; particularizar la arquitectura al **recorte del dominio asignado** (servicio, agregados, casos de uso, puertos y adaptadores, micro-frontend, datos locales, vecinos simulados); definir las reglas de arquitectura que se verificarán automáticamente; y registrar como **excepción propuesta** (`EXC-nnn`) cualquier punto donde la evidencia contradiga un ADR, sin desviarse por cuenta propia.

## Entradas

- `prompts/anexos/A1_ADR_COMUNES.md`, `A2_CONVENCIONES_BACKEND_JAVA21.md`, `A3_CONVENCIONES_FRONTEND_REACT.md`, `A4_FORMATO_CONTRATOS.md`, `A5_KIT_LOCAL_Y_RUTA.md`.
- `modernizacion/PARAMETROS.md` (dominio, recorte, nombre en inglés, paquete base, puertos).
- Salidas de las fases 01 y 02: reglas `BR-`, cálculos, procesos, comportamientos críticos, llamadas a vecinos.
- Kit base en el espacio de trabajo (`backend/`, `frontend/`, `local-stack/`, `contracts/`).
- Referencias que cite Wynxx sobre fronteras transaccionales, batch y pantallas del dominio (el COBOL no está disponible).

## Instrucciones

1. **Instala los ADR comunes.** Copia los 16 ADR de `A1` a `modernizacion/03-arquitectura/adr/ADR-001…016-*.md` con estado `Accepted (predefinido por facilitación)`. **No los modifiques.**
2. **Ficha de conformidad del dominio.** Para cada ADR indica cómo aplica al recorte y qué evidencia (`BR-`, `WXI-`) lo respalda o lo tensiona. Casos típicos a vigilar: (a) un paso que en el legado es **atómico con otro dominio** (ADR-001); (b) procesos **batch** (ADR-014); (c) cálculos con redondeo particular (ADR-006); (d) códigos de retorno que se resolvían en silencio (ADR-007); (e) **pantallas** del dominio (ADR-003) o ausencia de ellas; (f) tablas **compartidas** con otros dominios (ADR-005).
3. **Excepciones.** Si algún punto choca con un ADR, redacta `EXC-nnn` (evidencia, costo de cumplir, costo de apartarse, alternativa mínima) en `modernizacion/aprobaciones.md` y **detente a esperar** la respuesta del facilitador. Mientras tanto, sigue el ADR.
4. **Diseño del servicio** (`modernizacion/03-arquitectura/diseno-servicio.md`): nombre del servicio, responsabilidades, **agregados** y objetos de valor, **casos de uso** del recorte (uno por clase, con su `BR-`), puertos de entrada y de salida, adaptadores (web, `job` si hay batch, persistencia, vecinos), eventos de dominio, estructura de paquetes concreta (con `PAQUETE_BASE`), manejo de errores (códigos del catálogo) y transacciones.
5. **Diseño del micro-frontend** (`diseno-microfrontend.md`): pantallas o interfaz mínima de operación del recorte, rutas bajo `/<dominio>`, casos de uso de frontend, puertos y adaptadores, composición con el shell, mapeo de errores a mensajes en español.
6. **Vecinos.** Lista las dependencias **salientes** (otros dominios que este recorte invoca) y **entrantes** (quién invoca este servicio), con función y datos. Cada saliente será un puerto de salida con adaptador anticorrupción; en local se simula (ADR-013, prioridad baja).
7. **Datos locales (borrador).** Tablas del servicio con prefijo del dominio, claves y escala de importes; se afinan en la fase 09.
8. **Topología local** (`topologia-local.md`): componentes que se levantarán (PostgreSQL, backend, shell, micro-frontend, simulador opcional), puertos de `PARAMETROS.md`, orden de arranque y verificaciones de salud; coherente con `A5`.
9. **Reglas de arquitectura verificables.** Lista las pruebas de arquitectura del backend (ArchUnit) y las reglas de lint del frontend que se activarán (de `A2` y `A3`), y añade las específicas del dominio (p. ej. «el cálculo de penalidad solo existe en `domain.service`»).
10. **Trazabilidad de diseño**: tabla `BR- → capa y clase/módulo previstos`.

## Uso de OpenSpec

- Las decisiones de este prompt son **ADR ya aceptados**: es correcto reflejarlas en `openspec/config.yaml` (`context:`) con una referencia breve a `anexos/A1…A5` (idioma, trazabilidad, ADR comunes, reglas de capas).
- No se crean specs funcionales aún (fase 05). Define el nombre del *change* del recorte (kebab-case, `<dominio_en>-<recorte>`).

## Entregables

- `modernizacion/03-arquitectura/adr/ADR-001…016-*.md` (copiados, sin cambios).
- `modernizacion/03-arquitectura/ficha-conformidad.md`.
- `modernizacion/03-arquitectura/diseno-servicio.md` y `diseno-microfrontend.md`.
- `modernizacion/03-arquitectura/vecinos.md` y `datos-locales-borrador.md`.
- `modernizacion/03-arquitectura/topologia-local.md`.
- `modernizacion/03-arquitectura/reglas-de-arquitectura.md` (ArchUnit + lint, con las específicas del dominio).
- `modernizacion/03-arquitectura/trazabilidad-diseno.md`.
- Excepciones `EXC-nnn` en `modernizacion/aprobaciones.md` (si las hay).

## Criterios de aceptación

- [ ] Los 16 ADR están instalados sin modificar y marcados como predefinidos.
- [ ] La ficha de conformidad cubre cada ADR y cada tensión detectada está registrada como `EXC-nnn` con evidencia.
- [ ] Cada caso de uso del recorte está asignado a una clase/capa y enlazado a su `BR-`.
- [ ] El diseño respeta la regla de dependencia de ambas Clean Architecture (backend y frontend).
- [ ] Los vecinos están listados con dirección y función; ninguno requiere acceso a datos ajenos.
- [ ] La topología local usa los puertos de `PARAMETROS.md` y es consistente con `A5`.
- [ ] No se eligió ninguna tecnología distinta de las predefinidas.

## Trazabilidad

- `BR → caso de uso → clase/módulo → capa` y `BR → pantalla/ruta` en `trazabilidad-diseno.md` y en la matriz global.
- Cada `EXC-` enlaza los `BR-`/`WXI-` que la motivan.

## Restricciones

- No re-decidas ni «mejores» los ADR comunes; si hay un problema, registra una `EXC-nnn`.
- No introduzcas frameworks, librerías o patrones fuera de los anexos sin `EXC` aprobada.
- No escribas código de aplicación en esta fase.
- No diseñes acceso a datos de otros servicios.

## PUNTO DE APROBACIÓN

Solo si hay `EXC-nnn`: **DETENTE** y espera la respuesta del facilitador. Si no hay excepciones, basta una **revisión breve** del diseño del servicio y del micro-frontend por el equipo antes de pasar a la fase 04.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `03` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `04_MODELADO_DOMINIOS_DDD.md`
