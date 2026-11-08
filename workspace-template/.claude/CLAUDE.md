# Modo taller — Legacy Transform Lab (camino corto, 50 minutos)

Este archivo rige cuando se ejecuta un prompt de `prompts-corto/` (T0…T6). Si el usuario pide expresamente un prompt de `prompts/` (metodología completa), sigue ese prompt en lo que contradiga a este archivo.

**Objetivo único:** en ≤ 50 minutos, backend Java 21 y micro-frontend React corriendo en localhost para **un recorte vertical** de **un dominio**, construido con OpenSpec (spec-driven). Lo que no sirve a ese objetivo no se hace.

## Premisas (no se discuten ni se re-verifican)

1. **El dominio asignado es el del Excel** `wynxx-reasignacion-dominios` (columna «Dominio recomendado»). Que un archivo cambie de dominio respecto a Wynxx es esperado. No compares con la etiqueta original, no propongas dividir ni fusionar dominios, no abras un análisis de fronteras.
2. **Las reglas de negocio de `wynxx/` son la verdad.** No hay COBOL y no se busca. No hay verificación cruzada, ni catálogo de hechos `WXI-`, ni matriz de trazabilidad. La evidencia se cita como `wynxx/<archivo>#<sección>` (más `COB:` solo si Wynxx lo cita). Nivel máximo de evidencia: E2 (caso calculado). Nunca declares equivalencia.
3. **El alcance es un vertical slice:** 1–3 casos de uso y **como máximo 5 reglas `BR-`**. Todo lo demás es una lista de backlog de una línea por regla. No lo amplíes aunque el dominio sea pequeño.
4. **La arquitectura ya está decidida** (`prompts/anexos/A1`…`A5`): microservicio Java 21 / Spring Boot 3 / Maven con Clean Architecture; micro-frontend React + TypeScript + Vite con Clean Architecture cargado por `shell/`; OpenAPI 3.1 contract-first con errores RFC 9457; dinero como decimal (nunca `double`/`number`); PostgreSQL o H2 con Flyway. No la re-decidas, **no copies los ADR** al workspace, no redactes fichas de conformidad. Si algo la tensiona: una línea en `modernizacion/preguntas.md` y sigue el ADR.
5. **Vacíos, contradicciones y ambigüedades no bloquean.** Escribe una línea en `modernizacion/preguntas.md` (qué falta, supuesto adoptado) y continúa con el supuesto más conservador.
6. **Cero `DESV`.** Todo comportamiento del legado se preserva, incluidas las rarezas; las rarezas se anotan en una línea en `preguntas.md`.
7. **Los vecinos son stubs.** Se modelan como puertos de salida con adaptador anticorrupción y se simulan con `local-stack/simulator` (puerto 8090). No se modela ningún otro dominio.
8. **Una sola compuerta humana:** al final de T3 (specs). Todo lo demás se auto-aprueba: registra una línea en `modernizacion/aprobaciones.md` («auto-aprobado, modo taller») y sigue sin pedir permiso entre fases.
9. **Corriendo gana a completo.** Cada prompt tiene un tiempo objetivo. Si te pasas, **recorta alcance** (la regla o caso de uso de menor prioridad pasa al backlog), no estires el tiempo.
10. **El contrato OpenAPI es el punto de sincronía.** Congelado en T4, backend (T5A) y frontend (T5B) avanzan en paralelo y por separado. Si hay que cambiarlo, edita el YAML, avisa en `modernizacion/ESTADO.md` y regenera el cliente.

## Reglas de trabajo

- **No inventes.** Ni reglas, ni campos, ni códigos de error, ni comandos de OpenSpec o del kit. Lo que Wynxx no da es un vacío (premisa 5). Antes de usar un comando de OpenSpec que no hayas visto, `openspec <cmd> --help`.
- **Modernizar no es traducir:** reconstruye el comportamiento (reglas, cálculos, estados, errores), no la estructura de párrafos COBOL.
- **Trazabilidad mínima:** cada requisito de spec lleva una línea `Trazas: BR-… | wynxx/<archivo>#<sección> | E1/E2`. Cada regla en código lleva el comentario `// BR-xxx-nnn`. Cada prueba cita su BR. Nada más.
- **Dinero:** `BigDecimal` con escala y `RoundingMode` declarados junto a la regla (reproduce el orden de operaciones de la ficha). En el contrato y en el frontend, `Money = { amount: "string decimal", currency }`.
- **Permisos de trabajo ya asumidos:** puedes ejecutar sin preguntar `./mvnw`, `npm ci|run|test`, `openspec …`, `node`, `curl` a `localhost` y los scripts de `local-stack/scripts/`. No instales nada global, no uses Docker salvo que el participante lo pida, no toques nada fuera del workspace, no uses credenciales ni datos reales.
- **No escribas documentos que no estén en la lista de entregables del prompt.** En especial: no hay informes de cobertura, informes de implementación, matriz de trazabilidad, inventarios, catálogos de hechos, mapas de contextos ni ADR copiados.
- **Marca tareas de OpenSpec con `Edit`** (cambia `- [ ]` por `- [x]` en la línea exacta). Nunca reescribas `tasks.md` completo: dos sesiones (backend y frontend) pueden estar marcando a la vez.
- **Java:** exporta JDK 21 antes de Maven (`export JAVA_HOME=$(/usr/libexec/java_home -v 21)` en macOS; `local-stack/scripts/_lib.sh` ya lo resuelve).

## Contrato de salida de cada prompt

Al terminar, en **≤ 5 líneas**: qué se produjo (rutas), qué quedó fuera (backlog / preguntas), tiempo aproximado y cuál es el siguiente prompt. Sin secciones «Para aprobar» (salvo la compuerta de T3). Actualiza **una línea** de `modernizacion/ESTADO.md`.

## Mapa de archivos del modo taller

```
.claude/CLAUDE.md                       ← este archivo
prompts-corto/                          ← T0…T6 (qué ejecutar)
prompts/anexos/A1…A5                    ← arquitectura de referencia (solo leer)
wynxx/                                  ← export de Wynxx del dominio (entrada)
modernizacion/PARAMETROS.md             ← equipo, dominio, recorte, nombres, puertos
modernizacion/ESTADO.md                 ← una línea por prompt
modernizacion/aprobaciones.md           ← una línea por aprobación
modernizacion/preguntas.md              ← vacíos, supuestos y rarezas
modernizacion/01-recorte.md             ← T1: reglas BR del recorte + casos E2 + vecinos + backlog
modernizacion/02-diseno.md              ← T2: diseño del servicio y del micro-frontend
openspec/changes/<change>/              ← T3: proposal, specs, design, tasks
contracts/<dominio>.openapi.yaml        ← T4: contrato (con ejemplos de éxito y de cada error)
contracts/error-catalog.md              ← T4
contracts/neighbors/*.openapi.yaml      ← T4: solo los vecinos usados
backend/  frontend/  shell/             ← T0 (copiados del kit), T5A, T5B
modernizacion/evidencia-local.md        ← T6: salida real de verificar.sh y escenarios
```
