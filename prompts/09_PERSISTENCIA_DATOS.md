# Prompt 09 — Persistencia y migración de datos

> **Rol del agente:** Arquitecto de datos / ingeniero de datos  
> **Depende de:** Fases 04, 05 y 06 (Aprobadas); ADR de persistencia aceptado  
> **Produce:** Modelo de datos por contexto, mapeo legado↔nuevo, estrategia de migración y conciliación, y especificación de integridad

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

El legado usa DB2 (tablas con claves compuestas, `DECIMAL`, `DATE`, `TIMESTAMP`), archivos VSAM (KSDS/ESDS) y registros posicionales con `COMP-3`, `REDEFINES` (estructuras variables por producto) y longitudes fijas. Distintos programas acceden directamente a las mismas tablas. La persistencia moderna debe respetar la **propiedad de los datos por contexto** y preservar precisión numérica, integridad y semántica.

## Modo taller (dominio acotado)

**Base de datos pequeña y local.** ADR-005: PostgreSQL en contenedor por defecto (perfil `nodocker` con H2); migraciones **Flyway** en `backend/src/main/resources/db/migration`; **semillas deterministas** (SQL/CSV) derivadas de los casos E2; solo las tablas del recorte; no se usa Excel (un CSV versionado cumple la misma función). Prioriza un modelo simple que permita demostrar el flujo; la estrategia de migración de datos reales y la conciliación se documentan brevemente pero **no se ejecutan** en el taller.

## Objetivo

Diseñar el modelo de datos de cada contexto, el mapeo exacto desde las estructuras legadas, las reglas de integridad y numeración, la estrategia de migración y de conciliación (para demostrar que no se pierde ni altera información), y las pruebas de datos.

## Entradas

- Estructuras de datos que describan los artefactos de Wynxx (tablas, columnas, claves, copybooks de registro, VSAM, cargas); lo no descrito es un vacío.
- Entidades y estructuras de datos de Wynxx (`wynxx/`, `modernizacion/01-analisis/hechos/entidades.md`).
- `modernizacion/04-dominios/**` (agregados e invariantes), `modernizacion/02-funcional/integridad-datos.md`.
- ADR aceptado de persistencia (si no existe, DETENTE y propón).
- `prompts/anexos/A1_ADR_COMUNES.md` (ADR-005, 006) y `A2_CONVENCIONES_BACKEND_JAVA21.md` §6.

## Instrucciones

1. **Inventario de datos legados**: tablas (columnas, tipos, claves, índices, tablespace), archivos VSAM, copybooks de registro (con `REDEFINES`, `COMP-3`, `OCCURS`), parámetros en `parm/`. Marca quién escribe y quién lee cada una (por programa) para detectar **datos compartidos entre dominios**.
2. **Propiedad de datos**: asigna cada entidad a un único contexto propietario; para las compartidas define la relación (referencia por identificador, copia de lectura vía evento, consulta por contrato).
3. **Modelo por contexto** (lógico → físico con ADR): agregados, claves, unicidad, relaciones, índices necesarios para las consultas de las specs. No copies estructuras por conveniencia: modela por agregado.
4. **Mapeo campo a campo** `legado → nuevo`: tipo, longitud, escala, nulos, valores por defecto, codificaciones (productos, monedas, estados), formato de fecha, tratamiento de `REDEFINES` (¿qué variante aplica y cómo se decide?), `COMP-3` → decimal exacto, `SPACES/ZEROS` → nulo o valor. Registra **toda inconsistencia** de codificación detectada (p. ej. códigos distintos para el mismo concepto) como hallazgo y decide con negocio.
5. **Numeración y secuencias**: cómo se generan hoy (máximos + 1, contadores, claves compuestas por fecha/hora) y cómo se preserva unicidad y orden; qué pasa con concurrencia.
6. **Integridad y transacciones**: qué debe ser atómico (de la fase 02/03), versionado/optimismo, bloqueos, idempotencia.
7. **Estrategia de migración** (opciones, sin imponer): carga inicial, sincronización, ejecución en paralelo, corte. Define **conciliaciones** (conteos, sumas de control, saldos por cuenta/moneda, muestras) y criterios de éxito.
8. **Datos de prueba**: define cómo obtener/generar datos que ejerciten reglas y límites; cuáles requieren enmascaramiento.
9. **Retención y auditoría** de datos según lo evidenciado.

## Uso de OpenSpec

- Los **invariantes y requisitos de integridad observables** (unicidad, no negatividad, cierres) van como requisitos/escenarios en las specs del change; actualízalos con `/opsx:update <change>`.
- Las decisiones físicas (esquemas, tipos concretos) van en `design.md`, derivadas de ADR aceptados.
- Añade a `tasks.md`: creación del esquema, scripts de migración versionados, pruebas de conciliación.

## Entregables

- `modernizacion/09-datos/inventario-datos-legados.md` (con matriz escritor/lector por programa).
- `modernizacion/09-datos/modelo-<contexto>.md`.
- `modernizacion/09-datos/mapeo-legado-nuevo.md` (y `.csv`).
- `modernizacion/09-datos/estrategia-migracion-y-conciliacion.md`.
- `modernizacion/09-datos/hallazgos-datos.md` (inconsistencias, rarezas, dependencias).
- Specs/design/tasks del change actualizados y validados.

## Criterios de aceptación

- [ ] Cada tabla/archivo/campo legado tiene destino o decisión de descarte justificada.
- [ ] Cada entidad tiene un único contexto propietario.
- [ ] La conversión numérica conserva precisión y redondeo (casos de prueba con valores límite).
- [ ] Las inconsistencias de codificación están listadas con decisión o pregunta abierta.
- [ ] Hay criterios de conciliación medibles.
- [ ] `openspec validate --strict` pasa.

## Trazabilidad

- `COB/DB2:campo → campo nuevo → Requirement(integridad) → prueba de datos` (añade columna `Dato` a la matriz).

## Restricciones

- No elijas tecnología de persistencia sin ADR aprobado.
- No pierdas columnas «sin uso aparente» sin decisión explícita.
- No corrijas datos en origen ni modifiques el legado.
- No migres datos reales ni toques entornos: solo diseñas y generas artefactos.

## PUNTO DE APROBACIÓN

**DETENTE** si hay inconsistencias de codificación o decisiones de propiedad de datos sin aprobar; el modelo debe ser validado por el arquitecto y el responsable de datos antes de implementar el backend.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `09` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `07_IMPLEMENTACION_BACKEND.md`
