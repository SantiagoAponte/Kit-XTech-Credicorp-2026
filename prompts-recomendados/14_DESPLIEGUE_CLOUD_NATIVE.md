# Prompt 14 — Despliegue cloud-native (cuando corresponda)

> **Rol del agente:** Ingeniero de plataforma / DevOps  
> **Depende de:** Fases 03, 07, 10, 13 y 16; ADR de plataforma de despliegue aceptado  
> **Produce:** Estrategia y artefactos de empaquetado, entrega continua, infraestructura como código, configuración, ejecución batch, resiliencia y estrategia de migración

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

El legado se ejecuta en mainframe con ciclo batch nocturno (JCL), ventanas operativas y parámetros en miembros de datos. El despliegue moderno debe soportar tanto procesos en línea como procesos programados con las mismas garantías (orden, condiciones, reinicio, totales de control), y permitir una migración controlada. **La conveniencia de la nube y su forma deben justificarse**; no se asume.

## Modo taller (dominio acotado)

**Fase de extensión:** queda fuera del camino corto del taller (ver `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`). Ejecútala después del prompt 16, o solo si el equipo terminó con tiempo.

## Objetivo

Definir y generar, según las decisiones aprobadas, los artefactos de despliegue y operación: empaquetado, pipeline de CI/CD con las compuertas de calidad (specs válidas, pruebas, equivalencia), configuración por entorno, infraestructura como código, ejecución y reinicio de batch, resiliencia, estrategia de migración (paralelo/cutover) y plan de reversa.

## Entradas

- ADR aceptados (estilo, batch, persistencia, observabilidad, seguridad, migración).
- Código y pruebas (fases 07–12), controles de la fase 13 y **artefactos de contenerización y composición local de la fase 16** (punto de partida para las imágenes y el pipeline).
- Procesos batch y ventanas del legado (`jcl/`, `parm/`, `modernizacion/02-funcional/procesos.md`).
- Restricciones del cliente: plataforma permitida, regulación de residencia de datos, redes (si no existen, regístralas como vacío).

## Instrucciones

1. **Decisión de plataforma**: si no existe ADR aceptado, **propón** opciones (incluyendo no usar nube) con criterios (cumplimiento, costo, operación, capacidades del equipo) y DETENTE. No selecciones proveedor.
2. **Reutiliza la contenerización de la fase 16** (una imagen por servicio y por micro-frontend) y endurécela para entornos compartidos (versiones fijadas, escaneo, firmas si el ADR lo exige). Define las unidades de despliegue por el estilo arquitectónico aceptado y el pipeline: build, análisis estático, `openspec validate --all --strict`, pruebas (11/12), escaneo de dependencias, publicación, despliegue por entornos.
3. **Configuración y secretos**: separa configuración por entorno; migra parámetros del legado (`parm/`) solo según lo aprobado; sin secretos en el repositorio.
4. **Infraestructura como código**: define los recursos necesarios según el ADR; reproducible y versionado.
5. **Ejecución batch**: calendarización, dependencias entre trabajos, reintento/reinicio con checkpoint, límites, alertas y evidencias (totales de control) equivalentes a la cadena JCL.
6. **Resiliencia y escalado**: salud, reintentos, límites de tasa, degradación; dimensiona con datos reales de volumen si existen; si no, regístralo como vacío.
7. **Estrategia de migración**: opciones evaluadas (paralelo, strangler, cutover) con criterios de entrada/salida, conciliación (fase 09), monitoreo y **plan de reversa**.
8. **Runbooks y operación**: procedimientos de arranque/parada, incidentes, reproceso, cierre de día.
9. **Compuertas de calidad**: define las condiciones para promover entre entornos.

## Uso de OpenSpec

- Los requisitos **operativos observables** (ventanas de proceso, reinicio, retención, disponibilidad) se expresan como requisitos/escenarios en un change transversal (`operacion-y-despliegue`); valida con `openspec validate --strict`.
- Incluye en el pipeline la validación de OpenSpec (`openspec validate --all --strict`) y la verificación de que no hay changes sin archivar ni tareas pendientes en lo que se promueve.
- Implementa con `/opsx:apply` y, tras validación completa, archiva los changes (`/opsx:archive`) para que las specs principales reflejen el sistema desplegado.

## Entregables

- `modernizacion/14-despliegue/{estrategia,pipeline,configuracion,infraestructura,batch,resiliencia,migracion,runbooks}.md`.
- Definiciones de pipeline, empaquetado e infraestructura como código en la ubicación fijada por ADR.
- Plan de migración y reversa con criterios medibles.

## Criterios de aceptación

- [ ] El pipeline ejecuta specs válidas + pruebas + reglas de calidad y bloquea la promoción si fallan.
- [ ] El batch tiene calendarización, reinicio y totales de control equivalentes a los del legado.
- [ ] No hay secretos en el repositorio ni configuración duplicada por entorno.
- [ ] Existe plan de reversa probado en un entorno no productivo.
- [ ] Las decisiones de plataforma están respaldadas por ADR aceptados.

## Trazabilidad

- `JCL (paso/COND) → proceso programado → requisito operativo → prueba de operación`.

## Restricciones

- No elijas proveedor ni plataforma sin ADR aprobado.
- No despliegues en entornos reales ni uses credenciales: genera artefactos y valida localmente salvo autorización explícita.
- No asumas que cloud-native aplica a todo: justifícalo por componente.

## PUNTO DE APROBACIÓN

**DETENTE** ante cualquier decisión de plataforma, entorno o migración: requieren aprobación humana explícita registrada en `modernizacion/aprobaciones.md`.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `14` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `15_VALIDACION_FINAL_MODERNIZACION.md`
