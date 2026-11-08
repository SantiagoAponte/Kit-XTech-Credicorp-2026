# A5 — Kit base y ruta del taller (de los artefactos de Wynxx a backend y frontend corriendo en local)

> Meta del taller: que cada equipo vea **su backend Java corriendo y su frontend React corriendo**, funcionando para el pedazo de dominio que le tocó, contra una base de datos pequeña en contenedor.

## 1. Qué recibe cada equipo (el kit)
Preparado por facilitación **antes** del taller (el prompt `facilitadores/K0_CONSTRUIR_KIT_BASE.md` lo construye y verifica):

| Pieza | Contenido |
|---|---|
| `backend-template/` | Proyecto Java 21 + Spring Boot 3.x + Maven (con *wrapper*) con la estructura de capas de `A2`, un caso de uso de ejemplo (`Ping`), manejo de errores Problem Details, Flyway, perfiles `dev` / `nodocker`, pruebas de arquitectura ArchUnit y `Dockerfile` |
| `frontend-template/` | Micro-frontend React + TypeScript + Vite con las capas de `A3`, ruta de ejemplo, cliente generado desde OpenAPI, federación de módulos, lint de dependencias, Vitest y `Dockerfile` |
| `shell/` | Shell que carga micro-frontends (navegación, sesión de desarrollo, tema) |
| `local-stack/` | Composición con PostgreSQL + (opcional) backend + frontend + shell + simulador de vecinos; scripts de operación; plantilla de datos semilla; `.env.example` |
| `contracts-template/` | Plantilla OpenAPI + catálogo de errores |
| `openspec` | Inicializado, con `config.yaml` que referencia los ADR comunes |
| `prompts/` | Esta metodología |
| `paquete-offline/` (opcional) | Caché de dependencias e imágenes para redes lentas |

## 2. Ruta del taller (camino corto)

| Paso | Prompt | Qué hace el equipo | Resultado |
|---|---|---|---|
| 0 | — | Copia el kit, coloca el export de Wynxx del dominio en `wynxx/` y escribe `modernizacion/PARAMETROS.md` | Espacio de trabajo listo |
| 1 | **01** | Claude analiza los artefactos **del dominio asignado** | Hechos, vacíos, matriz v0 |
| 2 | **02** | Reconstrucción funcional del **recorte** del dominio | Reglas `BR-`, cálculos, casos E2 |
| 3 | **03** (predefinido) | Claude **aplica** la arquitectura común al dominio (no la re-decide) | Diseño del servicio y del micro-frontend |
| 4 | **04** (versión corta) | Agregados, objetos de valor, puertos y vecinos | Modelo del dominio |
| 5 | **05** | Un *change* de OpenSpec para el recorte | proposal, specs, design, tasks validados |
| 6 | **06** | Contratos OpenAPI y catálogo de errores | `contracts/` |
| 7 | **09** | Esquema, migraciones y datos semilla | Base de datos pequeña lista |
| 8 | **07** | Implementación del backend con `/opsx:apply` | `backend/` funcionando con pruebas |
| 9 | **08** | Implementación del micro-frontend | `frontend/` integrado en el shell |
| 10 | **16** | Levantar backend, frontend y base de datos en localhost y verificar | **Backend Java + frontend React corriendo** |
| — | 10–15 | Fase de **extensión** (integración entre dominios, validación profunda, seguridad, despliegue, informe final) | Fuera del camino corto |

## 3. Qué es un «recorte» del dominio
Con el tiempo del taller no se migra todo el dominio, sino un **recorte vertical**: de **1 a 3 procesos** (≈ **hasta 8 reglas `BR-`**) que formen un flujo demostrable de punta a punta (pantalla → API → regla → dato → respuesta). El resto queda en el backlog (`modernizacion/02-funcional/backlog-recorte.md`).

## 4. `modernizacion/PARAMETROS.md` (lo crea el equipo al empezar)
```markdown
# Parámetros del equipo
- EQUIPO: <nombre>
- DOMINIO: <nombre del dominio asignado en Wynxx>
- DOMINIO_EN: <nombre en inglés para código, p. ej. term-deposit>
- EXPORT_WYNXX: wynxx/<archivos del dominio>
- RECORTE: <procesos/reglas elegidos para el taller>
- PAQUETE_BASE: com.andes.<dominio_en>
- PUERTOS: backend 8080 · shell 5173 · micro-frontend 5174 · db 5432 · simulador 8090
- MODO_EJECUCION: directo | mixto | docker   (lo elige el participante en el prompt 16; puede cambiarlo después)
```

## 5. Ejecución local (resumen; el procedimiento completo está en el prompt 16)
El **participante elige** el modo cuando el agente se lo pregunta (tras detectar lo instalado):
- **Directo (sin Docker):** backend con JDK 21 + Maven (perfil `nodocker`, base H2) y frontend con Node (servidor de desarrollo).
- **Mixto:** solo PostgreSQL en contenedor (`local-stack`, perfil `db`); backend y frontend se ejecutan directamente. Reinicios rápidos; recomendado si Docker funciona.
- **Docker completo:** todo en contenedores (perfil `full`: base de datos + backend + micro-frontend + shell).
- **Scripts del kit** (nombres estándar; `levantar` acepta el modo: `levantar directo|mixto|docker`): `levantar`, `detener`, `reiniciar-limpio`, `estado`, `logs`, `cargar-datos`, `verificar`.
- **Verificación mínima:** salud del backend, shell cargado en el navegador, el micro-frontend visible bajo `/<dominio>`, y el flujo del recorte funcionando de punta a punta con datos semilla.

## 6. Presupuesto de tiempo (referencia para una sesión de 90 minutos)
| Tramo | Min | Notas |
|---|---|---|
| Discurso (repositorio, dominios y reglas de Wynxx) | 20 | Lo presenta facilitación |
| Prompts 01–03 | 15 | 03 es lectura/aplicación; el análisis ya viene acotado al dominio |
| Prompts 04–06 + 09 | 15 | Con plantillas del kit |
| Prompts 07–08 | 25 | Es el tramo crítico: usar el kit y el recorte pequeño |
| Prompt 16 y verificación | 10 | Mostrar backend y frontend corriendo |
| Cierre | 5 | |
Si el tiempo aprieta, prioriza: **01 → 02 → 03 → 05 → 07 → 08 → 16**. Los demás pueden hacerse después.

## 7. Plan B por equipo
- Si algo no arranca: cambiar de modo (por ejemplo, de `docker` o `mixto` a `directo`).
- Si el agente se bloquea: ver la **solución de referencia** del recorte (rama de facilitación) y comparar.
- Si la red es lenta: usar el `paquete-offline`.
