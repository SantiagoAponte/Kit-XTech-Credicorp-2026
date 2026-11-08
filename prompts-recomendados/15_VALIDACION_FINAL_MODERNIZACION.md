# Prompt 15 — Validación final de la modernización

> **Rol del agente:** Auditor técnico independiente (revisa, no construye)  
> **Depende de:** Todas las fases anteriores (fase 14 puede estar parcial si es un piloto)  
> **Produce:** Informe final de trazabilidad, cobertura, equivalencia (con nivel de evidencia), desviaciones, riesgos y decisión de continuidad

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

Cierre de un ciclo (normalmente un piloto con uno o varios dominios). El propósito no es afirmar que «todo está modernizado», sino demostrar con evidencia **qué está modernizado, cómo se sabe que conserva el comportamiento y qué falta**.

## Modo taller (dominio acotado)

**Fase de extensión:** queda fuera del camino corto del taller (ver `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`). Ejecútala después del prompt 16, o solo si el equipo terminó con tiempo.

## Objetivo

Auditar de extremo a extremo la cadena `Wynxx → reglas → especificaciones OpenSpec → componentes → pruebas de equivalencia`, emitir un veredicto por dominio basado en evidencia graduada, listar desviaciones, deuda y riesgos, y recomendar los siguientes pasos.

## Entradas

- Todo `modernizacion/**`, `openspec/` (specs principales y changes), código y pruebas, `modernizacion/aprobaciones.md`, `modernizacion/ESTADO.md`.
- `wynxx/` (para verificar cobertura contra la fuente).

## Instrucciones

1. **Integridad de la trazabilidad**: recorre la matriz fila por fila y comprueba que cada eslabón existe y es verificable (enlaces válidos, archivos y líneas correctas). Calcula: % de hechos Wynxx con regla, % de reglas con requisito, % de requisitos con componente, % con prueba, y por nivel de evidencia (E0–E3).
2. **Cobertura contra la fuente**: detecta hechos de Wynxx **sin destino** (no reconstruidos) y verifica que cada omisión está justificada (fuera de alcance, vacío, desviación aprobada).
3. **Estado de OpenSpec**: `openspec list --specs`, `openspec list --all`, `openspec validate --all --strict`; changes sin archivar, tareas pendientes, specs que no reflejan lo implementado.
4. **Reproducibilidad local**: comprueba que el entorno de la fase 16 levanta desde un clon limpio y que la evidencia de ejecución coincide con lo declarado.
5. **Equivalencia funcional**: consolida fases 11–12: por dominio, qué reglas están en E1/E2/E3, resultados de regresión, diferencias abiertas. Redacta la declaración con la fórmula: «*Para el dominio X se demostró equivalencia nivel Ei sobre N reglas (lista); no se demostró para M reglas (lista y motivo)*». **No uses «equivalencia completa» salvo que E3 cubra el alcance y la muestra esté descrita.**
6. **Desviaciones y decisiones**: lista todas las `DESV-`, `ADR-` y su estado de aprobación; confirma que no hay cambios de comportamiento sin aprobación.
7. **Vacíos y contradicciones**: estado final de `GAP/CON/AMB` (resueltos, abiertos y su riesgo).
8. **Calidad no funcional**: resumen de seguridad, observabilidad, despliegue y operación (fases 13–14) con brechas.
9. **Riesgos y deuda**: matriz con probabilidad/impacto y mitigación.
10. **Recomendación**: continuar con los siguientes dominios del backlog, qué ajustar en la metodología y qué evidencia adicional (datos, ejecuciones del legado, aprobaciones) se necesita para ascender de nivel.
11. **Resumen ejecutivo** de una página para el taller: qué se logró, con qué evidencia, qué se aprendió de usar Wynxx + OpenSpec + IA.

## Uso de OpenSpec

- Verificación: `openspec validate --all --strict`, `openspec list --specs`, `openspec list --all`, `openspec status --all`.
- Si todas las tareas de un change están completas y validadas, archívalo (`/opsx:archive` u `openspec archive <change>`) **solo tras aprobación humana**; confirma que `openspec/specs/` refleja el sistema modernizado.

## Entregables

- `modernizacion/15-validacion-final/informe-final.md`.
- `modernizacion/15-validacion-final/reporte-trazabilidad.md` (métricas + huecos).
- `modernizacion/15-validacion-final/declaracion-de-equivalencia.md` (por dominio, con nivel y alcance).
- `modernizacion/15-validacion-final/desviaciones-riesgos-deuda.md`.
- `modernizacion/15-validacion-final/resumen-ejecutivo.md` y `proximos-pasos.md`.

## Criterios de aceptación

- [ ] Cada métrica de trazabilidad está calculada con datos reales de la matriz.
- [ ] Toda omisión respecto de Wynxx está justificada o listada como pendiente.
- [ ] La declaración de equivalencia cita nivel de evidencia, alcance y muestra; no sobredeclara.
- [ ] `openspec validate --all --strict` pasa.
- [ ] No hay `DESV` sin aprobar implementadas.
- [ ] El resumen ejecutivo es coherente con el informe y refleja las limitaciones.

## Trazabilidad

- Reproduce la cadena completa `Wynxx → BR → Requirement → componente → prueba` para una muestra de reglas críticas como demostración navegable.

## Restricciones

- No implementes ni modifiques código en esta fase.
- No declares funcionalidades completas sin la evidencia correspondiente.
- No suprimas hallazgos negativos.
- No archives changes ni cierres fases sin aprobación humana.

## PUNTO DE APROBACIÓN

El informe final y la declaración de equivalencia se presentan al comité del taller. **La aceptación del piloto y la decisión de continuar son humanas.**

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `15` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `(fin del ciclo) — repetir 04→15 con los siguientes dominios del backlog`
