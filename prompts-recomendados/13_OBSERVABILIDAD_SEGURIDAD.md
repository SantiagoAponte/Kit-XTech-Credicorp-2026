# Prompt 13 — Observabilidad y seguridad

> **Rol del agente:** Ingeniero de plataforma / seguridad de aplicaciones  
> **Depende de:** Fases 03, 07 y 10  
> **Produce:** Requisitos y diseño de auditoría, logging, métricas, trazas, autenticación/autorización y protección de datos, preservando la semántica de auditoría del legado

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

El legado centraliza log y auditoría en un registro (VSAM) con niveles (informativo, advertencia, error, fatal, debug) y datos de contexto (cuenta, documento, importe, usuario, terminal, canal); enmascara datos sensibles con utilidades propias; controla facultades por monto y perfil; y cumple controles regulatorios (auditoría, prevención de lavado). Esa semántica de **qué se registra y quién puede hacer qué** debe conservarse, aunque la tecnología cambie.

## Modo taller (dominio acotado)

**Fase de extensión:** queda fuera del camino corto del taller (ver `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`). Ejecútala después del prompt 16, o solo si el equipo terminó con tiempo.

## Objetivo

Definir e implementar la observabilidad (logs estructurados, métricas, trazas, alertas) y la seguridad (identidad, autorización por facultades, protección de datos sensibles, auditoría inmutable) de la aplicación modernizada, sin perder los controles del legado y con seguridad por diseño.

## Entradas

- Hechos y reglas de seguridad/auditoría/log de Wynxx (log, enmascaramiento, facultades, accesos, cumplimiento).
- ADR aceptados de identidad, observabilidad y datos.
- Contratos (06) y datos (09) para identificar datos sensibles.

## Instrucciones

1. **Inventario de controles del legado**: qué se audita, con qué campos, en qué eventos; cómo se enmascara; qué facultades existen y por qué monto/perfil; bloqueos por intentos; expiración; bitácoras de acceso.
2. **Clasificación de datos**: documentos de identidad, números de cuenta/tarjeta, importes, datos de clientes; reglas de enmascaramiento y cifrado por dato.
3. **Auditoría**: especifica el registro de auditoría (qué eventos, qué campos, inmutabilidad, retención) preservando lo que el legado registra; mapea niveles de log legado ↔ nuevos.
4. **Autenticación y autorización**: modelo de roles/facultades equivalente al legado (incluida la regla de aprobación por monto), sin ampliar privilegios; gestión de sesión; manejo de intentos y bloqueos si el legado los tenía.
5. **Observabilidad**: logs estructurados con correlación entre contextos, métricas de negocio y técnicas (volúmenes, tasas de error por código de negocio, duración de procesos batch, totales de control), trazas distribuidas, alertas y tableros mínimos. Cada métrica de negocio referencia la regla que observa.
6. **Seguridad por diseño**: amenazas principales por contexto (STRIDE o equivalente aprobado), validación de entradas, gestión de secretos, dependencias, principio de mínimo privilegio, segregación de funciones.
7. **Cumplimiento**: mapa de requisitos regulatorios evidenciados en el legado → control en la nueva aplicación (no agregues normativas no evidenciadas; regístralas como vacío para el responsable de cumplimiento).
8. **Implementa** con `/opsx:apply` las tareas del change (instrumentación, autorización, enmascaramiento) y prueba cada control.

## Uso de OpenSpec

- Crea o actualiza un change transversal (p. ej. `observabilidad-y-seguridad-base`) con requisitos **observables** (qué se audita, quién puede ejecutar qué, qué se enmascara) y escenarios WHEN/THEN; valida con `openspec validate --strict`.
- Las decisiones técnicas (herramientas, formatos) viven en `design.md` y en ADR aceptados.

## Entregables

- `modernizacion/13-seguridad-observabilidad/{controles-legado,clasificacion-datos,modelo-autorizacion,plan-observabilidad,modelo-amenazas,mapa-cumplimiento}.md`.
- Change OpenSpec validado + implementación + pruebas de seguridad (autorización, enmascaramiento, auditoría).
- Matriz de trazabilidad: controles ligados a reglas y evidencia legada.

## Criterios de aceptación

- [ ] Todo evento auditado en el legado tiene equivalente documentado y probado.
- [ ] Los datos sensibles identificados se enmascaran/cifran según la regla y no aparecen en logs.
- [ ] La autorización reproduce las facultades del legado (pruebas positivas y negativas).
- [ ] Existen métricas y alertas para los procesos críticos y los totales de control batch.
- [ ] Los vacíos de cumplimiento están registrados, no suposiciones.

## Trazabilidad

- `WXI (log/seguridad) → BR/control → Requirement → implementación → prueba`.

## Restricciones

- No debilites controles por simplificar.
- No agregues requisitos regulatorios no evidenciados.
- No registres datos sensibles en claro ni en pruebas.
- No elijas herramientas sin ADR aprobado.

## PUNTO DE APROBACIÓN

El modelo de autorización, la clasificación de datos y el mapa de cumplimiento **requieren revisión** de seguridad y cumplimiento antes de considerar la fase cerrada.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `13` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `14_DESPLIEGUE_CLOUD_NATIVE.md`
