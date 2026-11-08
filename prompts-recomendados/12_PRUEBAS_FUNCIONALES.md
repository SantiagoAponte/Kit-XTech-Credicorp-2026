# Prompt 12 — Pruebas funcionales, de integración y de regresión

> **Rol del agente:** Ingeniero de pruebas (automatización)  
> **Depende de:** Fases 07, 09, 10, 16 y 11  
> **Produce:** Suite de pruebas automatizada (unitarias, integración, funcionales, regresión) trazable a requisitos y reglas, con informe de ejecución

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

La validación por regla (fase 11) demuestra equivalencia puntual. Esta fase construye la **red de seguridad automatizada** que protege el comportamiento durante toda la evolución: pruebas unitarias, de integración, funcionales (por escenario de spec), de contrato y de regresión, con casos positivos, negativos y límite derivados del legado cuando sea posible.

## Modo taller (dominio acotado)

**Fase de extensión:** queda fuera del camino corto del taller (ver `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`). Ejecútala después del prompt 16, o solo si el equipo terminó con tiempo.

## Objetivo

Completar y automatizar la estrategia de pruebas para cada dominio y proceso en alcance, asegurando que cada requisito OpenSpec tiene prueba ejecutable y que existe un conjunto de regresión estable reutilizable.

## Entradas

- Specs del change (escenarios WHEN/THEN), matriz de validación (fase 11), contratos (06), procesos e2e (10).
- Casos E2/E3 de las fases 02 y 11.
- ADR de herramientas de prueba (si no existe, propón y detente).
- Entorno local de la fase 16 (`modernizacion/16-ejecucion-local/`) como banco de pruebas funcionales y e2e.

## Instrucciones

1. **Estrategia de pruebas** (pirámide): unitarias de dominio, de componente, de contrato, de integración, funcionales por escenario, e2e y de regresión; define qué nivel cubre cada tipo de requisito.
2. **Mapa escenario → prueba**: un registro `Escenario → archivo de prueba → caso`. Todo escenario de toda spec debe tener prueba ejecutable; si falta, créala.
3. **Catálogo de casos**: positivos, negativos y límite (umbrales, cero, máximos, redondeos, fechas/feriados, monedas, estados inválidos, concurrencia/idempotencia, entradas malformadas). Cada caso cita su origen (`BR-`, `WXI-`) y nivel (E2/E3).
4. **Datos de prueba**: construye fixtures y generadores deterministas; si se usan datos reales, enmascarados y autorizados.
5. **Pruebas de regresión**: conjunto congelado de casos E2/E3 con resultados esperados exactos que debe pasar toda versión; ejecución automática y reporte de diferencias.
6. **Pruebas contra el sistema levantado**: las pruebas funcionales, de contrato e e2e se ejecutan también contra el entorno local de la fase 16 (API e interfaz), no solo con dobles en memoria.
7. **Pruebas batch**: entradas/salidas esperadas, totales de control, reinicio tras fallo, reproceso idempotente.
8. **Pruebas de no regresión de rarezas**: casos específicos para comportamientos `PRESERVAR`.
9. **Métricas**: cobertura de requisitos, de reglas `BR-` y de ramas de código; casos por nivel de evidencia.
10. **Ejecuta** la suite y entrega el informe con resultados reales (no simulados).

## Uso de OpenSpec

- El criterio de aceptación es la spec: cada escenario es un caso. Si descubres comportamientos no cubiertos, añade escenarios con `/opsx:update` (no solo código de prueba) y valida con `openspec validate --strict`.
- Marca en `tasks.md` las tareas de prueba cumplidas; comprueba el estado con `openspec status --change <change>`.

## Entregables

- Suite de pruebas automatizada en la ubicación definida por los ADR.
- `modernizacion/12-pruebas/estrategia-de-pruebas.md`, `mapa-escenario-prueba.md`, `catalogo-de-casos.md`.
- `modernizacion/12-pruebas/informe-ejecucion.md` (resultados reales, métricas, fallos).
- Conjunto de regresión versionado.

## Criterios de aceptación

- [ ] 100 % de los escenarios de las specs del alcance tienen prueba ejecutable.
- [ ] Cada `BR-` crítica tiene casos positivo, negativo y límite.
- [ ] La suite se ejecuta de forma reproducible y el informe muestra resultados reales.
- [ ] Las rarezas `PRESERVAR` están cubiertas por pruebas de regresión.
- [ ] Los casos indican nivel de evidencia; no se presentan casos E2 como demostración E3.

## Trazabilidad

- `Requirement/Scenario → prueba → resultado`, enlazado a `BR-` y a `WXI-`.
- Actualiza la matriz con la columna `Prueba` completa y el último resultado.

## Restricciones

- No elimines ni debilites pruebas para «poner en verde».
- No declares equivalencia completa por tener muchas pruebas: depende del nivel de evidencia.
- No uses datos reales sin autorización y enmascaramiento.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `12` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `13_OBSERVABILIDAD_SEGURIDAD.md`
