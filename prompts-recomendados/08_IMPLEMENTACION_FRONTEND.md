# Prompt 08 — Implementación del frontend (solo lo evidenciado por el legado)

> **Rol del agente:** Ingeniero frontend / UX funcional  
> **Depende de:** Fases 05, 06 y 07 (backend del contexto con contratos aprobados)  
> **Produce:** Interfaz de usuario de las funciones en línea del dominio piloto, equivalente en comportamiento a las pantallas del legado

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

El legado tiene pantallas BMS/CICS (mapas con campos, validaciones, mensajes, teclas y flujos de navegación) para operaciones de ventanilla. Si el alcance incluye funciones en línea, la nueva interfaz debe **preservar su comportamiento funcional** (qué datos se piden, validaciones, mensajes de error, permisos y flujos), no copiar su apariencia de terminal.

## Modo taller (dominio acotado)

Trabaja en el repositorio **`frontend/`** copiado de `frontend-template` del kit: **React + TypeScript**, **micro-frontend remoto** compuesto por el shell del kit, **Clean Architecture** según `prompts/anexos/A3_CONVENCIONES_FRONTEND_REACT.md`; cliente generado desde `contracts/<dominio>.openapi.yaml`.
**Excepción del modo taller (anula la regla «omite esta fase» y la de «no inventes pantallas»):** esta fase **no se omite**. Implementa las pantallas del recorte; si el legado no tiene pantalla para el recorte, crea una **interfaz mínima de operación** (formulario + resultado + errores) que ejerza los casos de uso, **marcándola explícitamente** como «interfaz de operación, no equivalente a una pantalla legada».

## Objetivo

Especificar e implementar la capa de interfaz de usuario únicamente para las funciones en línea del alcance, conectada a los contratos aprobados, con las mismas validaciones y mensajes de negocio, y con requisitos de accesibilidad y seguridad.

## Entradas

- Artefactos de Wynxx que describan las pantallas (BMS/CICS) del dominio en alcance (campos, validaciones, mensajes, flujos); si no las describen, regístralo como vacío.
- `openspec/changes/<change>` (specs de capacidades con interacción de usuario) y `modernizacion/06-contratos/**`.
- ADR aceptados de **arquitectura de software de frontend** (p. ej. shell + micro-frontends o frontend modular) y de tecnología de interfaz (si no existen, DETENTE y propón).
- `modernizacion/03-arquitectura/arquitectura-frontend.md` y el contrato del shell con los micro-frontends.
- `prompts/anexos/A1_ADR_COMUNES.md`, `A3_CONVENCIONES_FRONTEND_REACT.md` y `frontend/`, `shell/` (copias del kit).

## Instrucciones

1. **Inventario de pantallas**: por cada mapa BMS lista campos (nombre, longitud, obligatorio, protegido, numérico), teclas de función, mensajes, transacción CICS asociada y programa que la atiende.
2. **Mapa de flujo**: secuencia entre pantallas, condiciones, retornos y errores; permisos o facultades requeridas (p. ej. aprobación de supervisor por monto).
3. **Especificación**: si falta, añade/actualiza en el change las capacidades de interfaz como comportamiento observable (qué ve y puede hacer el usuario) con escenarios WHEN/THEN; actualiza con `/opsx:update <change>` y valida.
4. **Reglas de validación** idénticas a las del legado (formato, obligatoriedad, rangos); decide con la fase 02 si la validación vive en backend, frontend o ambos (el backend es la autoridad).
5. **Implementa** las tareas de interfaz del `tasks.md` con `/opsx:apply <change>` según la arquitectura de frontend aprobada (si es de micro-frontends: el micro-frontend del dominio como unidad independiente —construcción, versionado y despliegue propios— con su contrato hacia el shell, sin importar código de otros micro-frontends); usa los contratos de la fase 06 a través del gateway/BFF, no llamadas directas a datos.
6. **Comprueba que la interfaz carga y opera** integrada en el shell dentro del entorno local (prompt 16).
7. **Mensajes**: preserva el significado de cada mensaje de error de negocio y mapea al catálogo.
8. **Accesibilidad, seguridad y datos sensibles**: enmascara identificadores y números de cuenta como lo hace el legado; no registres datos sensibles en logs del cliente.
9. **Pruebas de interfaz** por escenario (flujo feliz, validaciones, errores, permisos).

## Uso de OpenSpec

- Las capacidades de interfaz se especifican como comportamiento observable en `specs/` del change (no se describen componentes ni librerías).
- Usa `/opsx:update` para ajustar artefactos y `/opsx:apply` para implementar; valida con `openspec validate --strict`.

## Entregables

- `modernizacion/08-frontend/inventario-pantallas.md` y `flujos.md`.
- Código de interfaz y pruebas (ubicación según ADR).
- `tasks.md` actualizado y completado para la parte de interfaz.
- Matriz de trazabilidad: columna `Componente` con pantallas.

## Criterios de aceptación

- [ ] Cada pantalla del alcance tiene equivalente funcional documentado y probado.
- [ ] Validaciones y mensajes de negocio coinciden con el legado (o `DESV` aprobado).
- [ ] No hay pantallas ni campos sin origen evidenciado.
- [ ] Los datos sensibles se muestran y registran como indica la fase 13.

## Trazabilidad

- `bms/<mapa>:campo → Requirement (interfaz) → componente → prueba de interfaz`.

## Restricciones

- No inventes pantallas ni flujos nuevos.
- No reproduzcas la estética de terminal: conserva el comportamiento.
- No elijas tecnología de interfaz sin ADR aprobado.
- Si el alcance no incluye funciones en línea, **omite esta fase** y regístralo en `ESTADO.md`.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `08` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `10_INTEGRACION_DOMINIOS.md`
