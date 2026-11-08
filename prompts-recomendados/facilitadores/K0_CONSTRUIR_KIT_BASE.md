# K0 — Construir y verificar el kit base del taller (solo facilitación)

> **Quién lo ejecuta:** facilitación, **antes** del taller, en una máquina con acceso a internet.
> **Para qué:** producir el kit que reciben los equipos (ver `../anexos/A5_KIT_LOCAL_Y_RUTA.md`) y **demostrar que funciona** antes de entregarlo.
> **Rol del agente:** Ingeniero de plataforma senior (Java 21, React, Docker). Construye de verdad y verifica de verdad.

## Reglas
1. Sigue exactamente los ADR de `../anexos/A1_ADR_COMUNES.md` y las convenciones de `A2`, `A3` y `A4`. No tomes decisiones nuevas de arquitectura; si algo no cuadra, repórtalo.
2. **No inventes versiones.** Consulta las versiones estables vigentes en fuentes oficiales (compatibles con Java 21 y con la línea Spring Boot 3.x) y **regístralas** en `KIT-VERSIONES.md` con fecha y fuente.
3. **No incluyas lógica de negocio de ningún dominio.** Solo un ejemplo mínimo (`Ping`) que demuestre las capas.
4. Sin secretos ni datos reales. Puertos solo en `localhost`.
5. **Antes de instalar o descargar algo** que no esté ya en la máquina (Maven *wrapper*, dependencias, imágenes), indica qué, de dónde y cuánto pesa, y espera confirmación.
6. Todo resultado de verificación debe ser **real** (salida de comandos). Si algo falla, repórtalo; no lo simules.

## Entregables (carpeta `kit/`)
- `backend-template/` — Java 21 + Spring Boot 3.x + Maven *wrapper*. Paquetes `domain / application / adapter(in,out) / infrastructure`; caso de uso `Ping` de extremo a extremo (puerto de entrada, caso de uso, controlador, DTOs); `ProblemDetailsAdvice` (RFC 9457); Flyway con `V1__init.sql`; perfiles `dev` (PostgreSQL) y `nodocker` (H2 en modo PostgreSQL); Actuator (salud); `correlation-id`; pruebas JUnit 5; **pruebas ArchUnit** de la regla de dependencia; prueba de contrato contra un OpenAPI de ejemplo; `Dockerfile` multietapa, usuario no root, `HEALTHCHECK`.
- `frontend-template/` — React + TypeScript estricto + Vite. Capas `domain / application / infrastructure / presentation`; composition root; ruta de ejemplo que invoca un caso de uso; cliente generado desde el OpenAPI de ejemplo; **federación de módulos** exponiendo `./routes`; modo independiente (`npm run dev`); lint de dependencias entre capas; Vitest; `Dockerfile`.
- `shell/` — host que carga el micro-frontend remoto (navegación, usuario de desarrollo, tema), configurable con la URL del remoto.
- `local-stack/` — composición con PostgreSQL (perfil `db`) y todo (perfil `full`: base de datos + backend + micro-frontend + shell); `healthcheck` y orden de arranque; `.env.example`; **scripts** `levantar`, `detener`, `reiniciar-limpio`, `estado`, `logs`, `cargar-datos`, `verificar` (agnósticos de SO cuando se pueda; indica los de macOS/Linux y Windows) que soporten **tres modos de ejecución**: `directo` (backend con JDK/Maven y frontend con Node, base H2, sin Docker), `mixto` (solo PostgreSQL en contenedor; apps directas) y `docker` (todo en contenedores); `levantar directo|mixto|docker`; plantilla de semillas (SQL/CSV); **simulador de vecinos** mínimo (servidor que sirve respuestas de ejemplo de un OpenAPI) en el puerto 8090.
- `contracts-template/` — OpenAPI 3.1 de ejemplo, plantilla de catálogo de errores.
- `openspec/` inicializado (`openspec init --tools <herramienta>`, comprobando la lista real con `openspec init --help`), con `config.yaml` que referencia los ADR comunes y las reglas de trazabilidad (`BR-`, `WXI-`; `COB:` solo si Wynxx lo cita).
- `README-KIT.md` — cómo copiar el kit, rellenar `modernizacion/PARAMETROS.md`, usar los scripts y el plan B.
- `KIT-VERSIONES.md` y `KIT-VERIFICACION.md`.
- `paquete-offline/` (opcional, si el facilitador lo pide) — caché de dependencias (`~/.m2`, `node_modules` o almacén de paquetes) e imágenes exportadas, con instrucciones de carga.

## Verificación (todo con evidencia real en `KIT-VERIFICACION.md`)
1. **Backend:** compila; pruebas verdes (incluida ArchUnit); arranca con perfil `nodocker`; `GET /actuator/health` responde correctamente; `POST` del caso `Ping` devuelve el cuerpo esperado; un error provocado devuelve Problem Details.
2. **Frontend:** instala dependencias, `build` y pruebas pasan; lint de dependencias pasa; arranca en modo independiente.
3. **Shell + remoto:** el shell carga el micro-frontend y la ruta de ejemplo renderiza; el cliente llama al backend local.
4. **Tres modos:** `levantar directo`, `levantar mixto` y `levantar docker` funcionan cada uno de punta a punta (salud en verde, flujo de ejemplo en el shell); `reiniciar-limpio` elimina estado; `detener` no deja procesos ni contenedores huérfanos; el cambio entre modos no exige tocar código ni configuración manual.
5. **Desde cero:** clona/copia el kit a una carpeta limpia y repite 1–4 siguiendo **solo** `README-KIT.md`. Anota tiempos y consumo aproximado de recursos.
6. **Prueba de ruptura de arquitectura:** introduce a propósito una dependencia prohibida (p. ej. el dominio importa una clase de Spring; un componente importa de `infrastructure`) y demuestra que **fallan** ArchUnit y el lint. Revierte el cambio.
7. **Matriz de compatibilidad:** versión de Java, Node y Docker probadas; sistemas operativos probados; qué no se pudo probar.

## Criterios de aceptación
- [ ] Todo se construye y arranca desde una carpeta limpia siguiendo solo el README.
- [ ] Existen evidencias reales de cada verificación; lo no probado está declarado.
- [ ] Las pruebas de arquitectura detectan violaciones (prueba de ruptura).
- [ ] No hay lógica de negocio, secretos ni datos reales.
- [ ] Las versiones usadas están registradas con fuente.
- [ ] Los **tres modos** (directo, mixto, docker) funcionan y se puede cambiar de uno a otro.

## Cierre
Entrega: resumen en 10 líneas, lista de archivos, resultados de verificación, riesgos (red, permisos, recursos de portátiles) y recomendación sobre el `paquete-offline`.
