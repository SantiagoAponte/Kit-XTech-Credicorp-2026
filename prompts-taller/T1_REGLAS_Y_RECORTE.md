# T1 — Reglas de negocio y recorte (≈ 6 min) · fusiona prompts 01 + 02

> **Rol:** analista funcional. Lees `wynxx/` y entregas **un solo documento** con el recorte listo para especificar.
> **Entrada:** `wynxx/**` (reglas y análisis del dominio), `modernizacion/PARAMETROS.md`. **Salida:** `modernizacion/01-recorte.md`.

## Hazlo

1. **Lista candidatas.** Lee los documentos de `wynxx/` y haz una tabla corta de **todas** las reglas de negocio que encuentres: `ID-origen (RN-… si Wynxx lo da) | enunciado en una línea | documento#sección | tipo (cálculo / estado / validación / proceso / consulta)`. Sin fichas aún.
2. **Elige el recorte.** Si `RECORTE` en `PARAMETROS.md` ya trae reglas, úsalo. Si no, propón **1–3 casos de uso y ≤ 5 reglas** que formen un flujo de punta a punta (pantalla → API → regla → dato → respuesta), con estos criterios en orden: (a) incluye al menos una regla con **cálculo o estado verificable**; (b) incluye una **consulta** para que el frontend muestre algo; (c) pocas dependencias de vecinos; (d) reglas corroboradas en más de una sección. Haz **una sola pregunta** al equipo para confirmar o ajustar y continúa con la respuesta. Anota el recorte en `PARAMETROS.md`.
3. **Ficha por regla del recorte** (`BR-<DOM3>-nnn`, donde `<DOM3>` es un prefijo de 3–4 letras del dominio). Campos, en una tabla o lista compacta: enunciado de negocio · precondiciones · entradas · **fórmula/lógica exacta** (operadores, orden, escala y modo de redondeo, base de días) · salidas · efectos (escrituras, llamadas a vecinos) · **errores con su código legado** · valores cableados (valor y dónde) · evidencia `wynxx/<archivo>#<sección>` (+ `COB:` solo si Wynxx lo cita) · nivel E1 (≥2 secciones lo corroboran o cita código) o E0 · mapeo `RN-legado ↔ BR`.
4. **Casos E2.** Por cada regla, **2 casos** (típico y límite) con entradas y resultado esperado. Si hay cálculo, calcúlalo con un script corto (Python/Node) y pega los valores; no los estimes de cabeza.
5. **Vecinos.** Tabla: vecino · función que se le pide · datos que viajan · entrante/saliente · criticidad. Solo los del recorte.
6. **Backlog.** Una línea por regla fuera del recorte: `ID-origen | enunciado corto | por qué quedó fuera`. Nada de fichas.
7. **Preguntas y rarezas.** Agrega a `modernizacion/preguntas.md` (≤ 10 líneas): vacío o ambigüedad · supuesto adoptado · regla afectada. Las rarezas del legado (valores cableados, comparaciones raras, errores silenciosos) se **preservan** y se anotan aquí.

## Estructura de `modernizacion/01-recorte.md`

`1 Recorte y casos de uso (UC-1…) · 2 Fichas BR · 3 Casos E2 · 4 Vecinos · 5 Backlog`

## No hagas

Catálogo de hechos `WXI-`, verificación cruzada, matriz de trazabilidad, `DESV`, clasificación PRESERVAR/DECIDIR/CORREGIR, análisis de dominios, reglas fuera del recorte con ficha. Máximo ≈ 150 líneas en total.

## Salida

Cierre en ≤ 5 líneas y siguiente: `T2_DISENO.md`. Actualiza la línea T1 de `ESTADO.md`.
