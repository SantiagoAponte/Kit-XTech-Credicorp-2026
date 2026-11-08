# T2 — Diseño del servicio y del micro-frontend (≈ 5 min) · fusiona prompts 03 + 04

> **Rol:** arquitecto que **aplica** la arquitectura común (no la decide). Un solo documento corto.
> **Entrada:** `modernizacion/01-recorte.md`, `PARAMETROS.md`, `prompts/anexos/A2` (backend), `A3` (frontend), `A4` (contratos) — solo leer; los ADR se citan por número (`ADR-002`…), **no se copian**.
> **Salida:** `modernizacion/02-diseno.md` (≤ 120 líneas).

## Secciones obligatorias

1. **Servicio.** Nombre (`<dominio-en>-service`), `PAQUETE_BASE`, responsabilidades en 3 líneas.
2. **Casos de uso.** Tabla: `UC | clase (una por caso) | BR que implementa | operación HTTP (método y ruta bajo /api/v1) | idempotente (sí/no)`. Operaciones que crean o mueven dinero llevan `Idempotency-Key` (A4).
3. **Dominio.** Agregados y raíces, objetos de valor (`Money`, identificadores, enumerados de estado), servicios de dominio (p. ej. el calculador de la fórmula) e **invariantes con su `BR-`**. Solo lo que el recorte necesita.
4. **Puertos y adaptadores.** Puertos de entrada y de salida; para cada vecino del recorte, un puerto de salida + adaptador anticorrupción (`adapter.out.<vecino>`), indicando que en local lo sirve el simulador (`:8090`). Persistencia: agregado → tabla.
5. **Datos.** Tablas con prefijo del dominio, claves, tipo y escala de importes (según la ficha BR), índices que pidan las consultas. Es un borrador que T4 vuelve SQL.
6. **Errores.** Prefijo de código (`<DOM3>-nnnn`), tabla `código legado → código nuevo → HTTP` para los errores del recorte (400/404/409/422/503 según A4).
7. **Micro-frontend.** Rutas bajo `RUTA_SHELL`; una pantalla por caso de uso (consulta + formulario + resultado + errores). Si Wynxx no describe pantalla para el recorte, rotúlala **«interfaz de operación, no equivalente a una pantalla legada»**. Casos de uso del frontend, puerto `…Gateway`, mapeo `code → mensaje en español`.
8. **Trazabilidad de diseño.** Tabla `BR → clase de dominio / caso de uso → capa`.

## Reglas

- Respeta la regla de dependencia de A2 y A3. No añadas librerías ni patrones fuera de los anexos.
- Una tensión con un ADR se anota en una línea en `preguntas.md` y **se sigue el ADR**; no se redacta `EXC`.
- Sin código de aplicación en esta fase.

## Salida

Cierre en ≤ 5 líneas y siguiente: `T3_SPECS_OPENSPEC.md`. Actualiza la línea T2 de `ESTADO.md`.
