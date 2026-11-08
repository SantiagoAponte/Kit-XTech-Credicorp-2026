# Prompt 11 — Validación de reglas de negocio (equivalencia a nivel de regla)

> **Rol del agente:** Ingeniero de calidad funcional / analista de equivalencia  
> **Depende de:** Fases 07, 09 y 16 (y 10 si hay procesos e2e); reglas aprobadas en fase 02  
> **Produce:** Matriz regla→implementación→prueba, resultado de validación por regla, evidencia de equivalencia con su nivel (E1–E3) y registro de brechas

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

Antes de probar «funcionalmente» hay que demostrar, regla por regla, que lo implementado hace lo mismo que el legado. La evidencia tiene grados: leer el código (E1), derivar casos (E2) o contrastar con ejecución real o datos reales del legado (E3). **Solo E3 permite hablar de equivalencia demostrada**; lo demás es evidencia parcial y debe declararse como tal.

## Modo taller (dominio acotado)

**Fase de extensión:** queda fuera del camino corto del taller (ver `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`). Ejecútala después del prompt 16, o solo si el equipo terminó con tiempo.

## Objetivo

Verificar que cada regla `BR-` del alcance está implementada, probada y con el nivel de evidencia más alto disponible; identificar reglas no cubiertas, comportamientos distintos y brechas; y emitir un veredicto honesto por regla y por dominio.

## Entradas

- `modernizacion/02-funcional/reglas-negocio.md`, `calculos.md`, `comportamientos-criticos.md`.
- Código y pruebas de las fases 07–10; `openspec/changes/<change>` (specs); entorno local de la fase 16 (para contrastar resultados contra el sistema en ejecución).
- Datasets de regresión, extractos reales anonimizados o salidas del legado **si el humano los aporta** (habilitan E3). Sin ellos, el nivel máximo alcanzable es E2.
- Opcional y solo si el facilitador lo autoriza: casos de referencia independientes del repositorio de entrenamiento (`docs-taller/dominios/<DOM>.md`, apartado *casos dorados*). **No son artefactos de Wynxx**; son un oráculo de contraste y están calculados con script, no ejecutando COBOL (nivel E2).

## Instrucciones

1. **Matriz de validación por regla**: para cada `BR-`: requisito/escenarios que la cubren, código que la implementa, pruebas que la ejercen, nivel de evidencia actual y brecha para llegar al siguiente nivel.
2. **Reglas sin cobertura**: detecta `BR-` sin requisito, sin código o sin prueba y repórtalas como brechas; no las ocultes.
3. **Revisión de la implementación vs. reglas documentadas**: para cada regla crítica compara la lógica implementada con la ficha `BR-` y con lo que cita Wynxx (orden de operaciones, tipos, redondeo, límites, valores cableados, tratamiento de errores). Anota diferencias (`DIF-nnn`) y clasifícalas: *equivalente* · *diferencia intencional (con DESV aprobado)* · *diferencia no justificada* · *no determinable*.
4. **Validación de cálculos**: ejecuta los ejemplos E2 de la fase 02 contra la implementación y compara resultado exacto (centavo a centavo). Añade casos límite: cero, mínimo, máximo, justo en el umbral, redondeos .5, cambio de moneda, fin de mes/fin de año, días hábiles vs. feriados.
5. **Validación de estados y transiciones**: pruebas de cada transición permitida y de las prohibidas.
6. **Validaciones y errores**: cada validación con su caso positivo y negativo; verifica el código de error esperado.
7. **Comportamientos críticos**: para los `PRESERVAR` demuestra que se conservan; para `DECIDIR/CORREGIR` confirma que no se implementó cambio sin `DESV` aprobado.
8. **Si hay datos reales o salidas del legado (E3)**: ejecuta la comparación (misma entrada → misma salida) sobre una muestra definida y reporta tasa de coincidencia y cada discrepancia con su diagnóstico. Si no hay, declara explícitamente `Equivalencia E3: no demostrable con la evidencia actual` y lista qué datos faltan.
9. **Veredicto** por regla y por dominio: `Cubierta E1/E2/E3`, `Cubierta parcial`, `No cubierta`, `Diferencia`. **Prohibido** escribir «equivalencia completa» sin E3 suficiente y explicado.

## Uso de OpenSpec

- Usa los **escenarios de las specs** como casos de prueba oficiales; si falta un caso límite, agrégalo a la spec con `/opsx:update <change>` (no solo a la prueba) y valida con `openspec validate --strict`.
- Si el change tiene el flujo `verify` habilitado (se activa con `openspec config profile`), úsalo; en caso contrario realiza la verificación manualmente y documéntala. No asumas que existe.
- Mantén `tasks.md` al día (tareas de prueba marcadas).

## Entregables

- `modernizacion/11-validacion/matriz-validacion-reglas.md` y `.csv`.
- `modernizacion/11-validacion/diferencias.md` (`DIF-nnn`).
- `modernizacion/11-validacion/resultados-calculos.md`.
- `modernizacion/11-validacion/brechas-y-evidencia-faltante.md` (qué datos/ejecuciones faltan para subir a E3).
- `modernizacion/11-validacion/veredicto-por-dominio.md`.

## Criterios de aceptación

- [ ] Cada `BR-` del alcance tiene fila con nivel de evidencia y veredicto.
- [ ] Los cálculos críticos coinciden con los ejemplos E2 al centavo, o la diferencia está explicada.
- [ ] Las diferencias no justificadas están abiertas como defecto o `DESV` propuesto.
- [ ] El informe declara con claridad el nivel máximo de evidencia alcanzado y no afirma E3 sin datos.

## Trazabilidad

- Completa en la matriz las columnas `Prueba` y `Nivel-evidencia` con enlaces a pruebas y resultados.
- Cadena verificable: `WXI → BR → Requirement → código → prueba → resultado`.

## Restricciones

- No ajustes la prueba para que coincida con el código sin justificar contra la regla documentada (`BR-`).
- No declares equivalencia completa sin E3.
- No ejecutes ni modifiques el legado salvo que el humano aporte salidas del mismo.
- No ocultes brechas.

## PUNTO DE APROBACIÓN

Entrega el veredicto y la lista de diferencias. Las **diferencias no justificadas** requieren decisión humana (corregir el código, aprobar `DESV` o reabrir la regla). Registra la decisión.

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `11` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `12_PRUEBAS_FUNCIONALES.md`
