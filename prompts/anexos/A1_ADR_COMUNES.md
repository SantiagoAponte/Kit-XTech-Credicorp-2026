# A1 — ADRs comunes del taller (arquitectura predefinida)

> **Estado de todos los ADR de este documento: `Accepted (predefinido por facilitación)`.**
> Todos los equipos construyen sobre esta misma arquitectura. **No se re-deciden.** Si la evidencia del dominio asignado tensiona un ADR, el agente registra una **excepción propuesta** `EXC-nnn` y se detiene a esperar aprobación; nunca se desvía por su cuenta.
> Las versiones exactas de librerías y herramientas las fija el **kit base** (ver `A5_KIT_LOCAL_Y_RUTA.md`); aquí se decide el *qué* y el *porqué*, no el número de versión.

| ADR | Decisión | En una línea |
|---|---|---|
| 001 | Estilo de sistema | Microservicios: un servicio por dominio (contexto acotado) |
| 002 | Backend | Java 21 + Spring Boot 3.x + Maven, con Clean Architecture |
| 003 | Frontend | React + TypeScript + Vite, micro-frontend remoto, con Clean Architecture |
| 004 | Contratos | OpenAPI 3.1 *contract-first*, errores RFC 9457, catálogo de errores |
| 005 | Datos | Base de datos por servicio; PostgreSQL en contenedor; H2 como alternativa sin Docker |
| 006 | Dinero y números | `BigDecimal` con escala y redondeo explícitos; nunca coma flotante |
| 007 | Errores y validación | Excepciones de dominio → Problem Details con código estable |
| 008 | Estrategia de pruebas | Pirámide + pruebas de arquitectura + pruebas de contrato |
| 009 | Contenerización y ejecución local | Docker disponible, una imagen por unidad; **el participante elige el modo de ejecución local** (directo, mixto o Docker) |
| 010 | Observabilidad mínima | Salud, logs estructurados, correlación |
| 011 | Seguridad en local | Usuario de desarrollo explícito; sin secretos en el repositorio |
| 012 | Estructura de repositorios | Espacio de trabajo + `backend/` + `frontend/` + `local-stack/` |
| 013 | Dominios vecinos | Simulados por contrato (prioridad baja) |
| 014 | Procesos batch | Casos de uso invocables por adaptador de entrada `job` |
| 015 | Nomenclatura e idioma | Código en inglés, glosario es↔en, mensajes al usuario en español |
| 016 | Nube | Destino cloud-native neutral de proveedor; fuera del alcance de construcción del taller |

---

## ADR-001 — Estilo de sistema: microservicios por dominio
- **Contexto.** Los dominios del legado tienen reglas, datos y ritmo de cambio propios, pero comparten núcleos (posteo, calendario, log).
- **Decisión.** Cada dominio asignado se moderniza como **un microservicio** con su propia base de datos y su propio micro-frontend. Los núcleos compartidos se tratan como dependencias externas (simuladas en el taller).
- **Consecuencias.** Se necesita contrato explícito con cada vecino (ADR-004); no hay acceso a datos de otros servicios; la consistencia entre servicios es eventual.
- **Excepción esperable.** Si dos pasos del dominio exigen atomicidad *dentro del legado* con otro dominio, regístralo como `EXC`: la solución habitual es mantenerlos dentro del mismo servicio, no distribuir la transacción.

## ADR-002 — Backend: Java 21, Spring Boot 3.x, Maven, Clean Architecture
- **Decisión.** Java 21 (LTS), Spring Boot 3.x (línea estable compatible con Java 21, versión fijada por el kit), Maven (con *wrapper*). Arquitectura **Clean Architecture** con cuatro capas y regla de dependencia hacia el dominio (detalle en `A2`):
  `domain` ← `application` ← `adapter` (in/out) ← `infrastructure/bootstrap`.
- **Reglas.** El dominio no importa Spring, JPA, Jackson ni nada de infraestructura. Los casos de uso dependen de **puertos** (interfaces); los adaptadores los implementan. Se verifica con pruebas de arquitectura (ADR-008).
- **Prácticas obligatorias.** Un caso de uso por clase; objetos de valor inmutables (`record`); agregados que protegen invariantes; transacción en el caso de uso; mapeo explícito entre capas; sin lógica de negocio en controladores ni en entidades de persistencia.

## ADR-003 — Frontend: React + TypeScript + Vite, micro-frontend, Clean Architecture
- **Decisión.** React con TypeScript (modo estricto) y Vite. El frontend del dominio es un **micro-frontend remoto** que un **shell** (aportado por el kit) compone mediante **federación de módulos**. Clean Architecture también en frontend con capas `domain` / `application` / `infrastructure` / `presentation` (detalle en `A3`).
- **Reglas.** Los componentes React (presentación) no llaman a `fetch` directamente: usan casos de uso; el cliente de API se genera o se escribe a partir del contrato OpenAPI (ADR-004); un micro-frontend **no importa código de otro**; dependencias compartidas (React, router) como *singletons* declarados en el kit.
- **Nota.** La validación de negocio autoritativa vive en el backend; el frontend valida para dar respuesta inmediata, replicando reglas simples del contrato.

## ADR-004 — Formato de contratos
- **Decisión.** **OpenAPI 3.1** *contract-first* en `contracts/<dominio>.openapi.yaml`; errores en **RFC 9457 (Problem Details)** con `code` estable del catálogo; versionado en la ruta (`/api/v1`); paginación y fechas ISO-8601; dinero como objeto `{ amount: "string decimal", currency: "PEN|USD|EUR" }`; cabecera `Idempotency-Key` en operaciones que crean o mueven dinero. Eventos (si el dominio los emite): **AsyncAPI + JSON Schema**, solo como documentación en el taller.
- **Pruebas.** El backend valida sus respuestas contra el contrato; el frontend usa un cliente tipado derivado del contrato.
- **Detalle y ejemplos:** `A4_FORMATO_CONTRATOS.md`.

## ADR-005 — Datos
- **Decisión.** **Base de datos por servicio.** Local: **PostgreSQL** en contenedor (por defecto) con migraciones **Flyway** versionadas. Alternativa sin Docker: perfil `nodocker` con **H2** en modo de compatibilidad PostgreSQL. Persistencia con Spring Data JPA **solo en el adaptador de salida**, con entidades de persistencia **separadas** del modelo de dominio.
- **Datos semilla.** Scripts SQL/CSV deterministas derivados de los casos E2 de la fase 02. No se usan datos reales. (No se usa Excel: un CSV semilla cubre la misma necesidad y se versiona.)
- **Prohibido.** Acceder a las tablas de otro servicio.

## ADR-006 — Dinero y números
- **Decisión.** Importes y tasas en `BigDecimal` con **escala y `RoundingMode` explícitos por regla** (documentados en la ficha `BR-`); prohibido `double`/`float`. Fechas con `java.time`. Conversión de monedas del legado: `604→PEN`, `840→USD`, `978→EUR`.
- **Equivalencia.** El orden de operaciones y el redondeo del legado (p. ej. `COMPUTE … ROUNDED`, half-up, truncado), **tal como los documenta la ficha `BR-`**, se reproducen y se prueban con los valores E2.

## ADR-007 — Errores y validación
- **Decisión.** El dominio lanza excepciones de dominio con un `code` del catálogo (sin dependencias de framework); el adaptador web las traduce a Problem Details; Bean Validation solo en el borde (DTOs de entrada). Los códigos de retorno del legado se mapean a códigos nuevos en un catálogo bidireccional.
- **Prohibido.** Errores silenciosos, salvo que la fase 02 haya clasificado el comportamiento como `PRESERVAR` y quede documentado.

## ADR-008 — Estrategia de pruebas
- **Backend.** Dominio: JUnit 5 sin Spring. Aplicación: con dobles de puertos. Adaptadores: pruebas de capa web y de persistencia (contenedor opcional). **Pruebas de arquitectura (ArchUnit)** que impiden violar la regla de dependencia. **Pruebas de contrato** contra el OpenAPI.
- **Frontend.** Vitest + Testing Library para casos de uso y componentes; reglas de dependencias entre capas verificadas por lint; prueba de integración con el shell en el entorno local.
- **Trazabilidad.** Cada prueba cita su `BR-` y escenario OpenSpec.

## ADR-009 — Contenerización y ejecución local
- **Decisión.** **Docker**: una imagen por unidad desplegable (backend y micro-frontend), construcción multietapa, usuario no root, `healthcheck`, configuración por variables de entorno, sin secretos en la imagen. `local-stack/` aporta una composición con perfiles (`db` = solo base de datos; `full` = todo).
- **Modo de ejecución local elegido por el participante** (el agente detecta lo instalado, pregunta y registra la respuesta en `PARAMETROS.md`): **directo** (backend con JDK y Maven, frontend con Node, base H2 con perfil `nodocker`, sin Docker); **mixto** (solo PostgreSQL en contenedor; backend y frontend directos); **docker** (todo en contenedores, composición `full`). Los scripts del kit soportan los tres y se puede cambiar de uno a otro sin tocar código.
- **Puertos por defecto** (los fija el kit): backend `8080`, shell `5173`, micro-frontend del dominio `5174`, PostgreSQL `5432`, simulador de vecinos `8090`.

## ADR-010 — Observabilidad mínima
- **Decisión.** Endpoints de salud/estado (Spring Actuator), logs estructurados con `correlation-id` propagado por cabecera, métricas básicas (Micrometer). Los eventos de auditoría que registraba el legado (qué, quién, cuándo, importe) se conservan como registro de auditoría del dominio.

## ADR-011 — Seguridad en local
- **Decisión.** Sin proveedor de identidad en el taller: un **usuario de desarrollo** inyectado explícitamente (perfil `dev`), claramente marcado como no apto para producción; CORS limitado a `localhost`; **sin secretos ni credenciales reales** en el repositorio (`.env.example` con valores de desarrollo). La autorización por facultades del legado se modela en el dominio (regla) aunque la autenticación esté simulada.

## ADR-012 — Estructura de repositorios

```
equipo-<dominio>/                ← espacio de trabajo (repositorio de especificación)
├── wynxx/                       ← export de Wynxx del dominio asignado
├── prompts/                     ← esta metodología
├── openspec/                    ← specs y changes
├── modernizacion/               ← documentos de trabajo, ADR, matriz de trazabilidad
├── contracts/                   ← OpenAPI/AsyncAPI del dominio
├── local-stack/                 ← composición, scripts, semillas, simulador de vecinos
├── backend/                     ← repositorio 1: Java 21, Clean Architecture
└── frontend/                    ← repositorio 2: React, micro-frontend
```
`backend/` y `frontend/` son repositorios independientes (pueden tener su propio `git`). El código COBOL original **no** forma parte de este espacio de trabajo: la fuente son los artefactos de Wynxx (`wynxx/`); las referencias al código (`COB:archivo:línea`) se registran solo cuando Wynxx las cita.

## ADR-013 — Dominios vecinos
- **Decisión.** Las llamadas salientes a otros dominios se definen como **puertos de salida** con su contrato. En local se resuelven con un **simulador** (respuestas fijas derivadas del contrato) servido en `8090`. **Prioridad baja**: primero debe funcionar el dominio asignado con datos propios; el simulador se añade solo si el caso lo requiere.

## ADR-014 — Procesos batch
- **Decisión.** Los procesos batch del dominio se implementan como **casos de uso de aplicación**, invocables por un adaptador de entrada `job` (ejecución a demanda y, si procede, programada). En local se exponen mediante un endpoint interno protegido por el perfil `dev` para poder demostrarlos. Se conservan totales de control y condiciones de parada del JCL.

## ADR-015 — Nomenclatura e idioma
- **Decisión.** Identificadores de código en **inglés**, con un **glosario es↔en** del lenguaje ubicuo (cada término legado en español ↔ nombre en código ↔ `BR-` relacionadas) para no perder trazabilidad; mensajes al usuario en **español**; commits con formato convencional.

## ADR-016 — Nube
- **Decisión.** El destino es cloud-native (imágenes OCI, configuración externa, servicios sin estado, observabilidad), **neutral de proveedor**. El taller **no** despliega en nube; deja preparadas las imágenes y la configuración.

---

## Cómo gestionar una excepción (`EXC-nnn`)
1. Describe la evidencia del dominio que tensiona el ADR (cita `BR-` y `WXI-`).
2. Explica el costo de cumplir el ADR y el de apartarse.
3. Propón la alternativa mínima.
4. **Detente** y registra en `modernizacion/aprobaciones.md`. Hasta que el facilitador responda, se sigue el ADR.
