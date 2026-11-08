# T5A — Backend (≈ 16 min, en paralelo con T5B) · prompt 07

> **Rol:** desarrollador backend Java 21. Trabajas **solo en `backend/`** (y marcas tareas `[BE]`). Otra sesión hace `frontend/` a la vez: no la toques.
> **Entrada:** change aprobado (`tasks.md` `[BE]`), `contracts/`, `modernizacion/01-recorte.md` (casos E2), `02-diseno.md`, `prompts/anexos/A2`.

## Hazlo (con `/opsx:apply <change>` limitado a las tareas `[BE]`)

1. **Esqueleto caminante primero (≈ 5 min).** Implementa el **primer caso de uso de punta a punta** (dominio → caso de uso → controlador → persistencia → migración/semilla) y deja `./mvnw -q verify` en verde antes de seguir. Retira del kit el ejemplo `Ping` (modelo, casos de uso, controlador, DTO, entidad, pruebas y semilla) a medida que lo reemplazas; **conserva** `ProblemDetailsAdvice`, `CorrelationIdFilter`, `Money`, la idempotencia y la prueba de arquitectura.
2. **Resto de casos de uso**, uno por uno, en el orden de `tasks.md`. Cada uno: clase de caso de uso con un solo método público, lógica en `domain` (sin Spring/JPA), controlador delgado, DTOs fuera del dominio.
3. **Reglas.** Cada `BR-` en un único lugar de dominio con el comentario `// BR-xxx-nnn` y, junto a las constantes numéricas, su escala y `RoundingMode`. Dinero siempre `BigDecimal` (nunca desde `double`), reproduciendo el orden de operaciones de la ficha. Reloj inyectado (`ClockPort`), nunca `LocalDate.now()` en dominio/aplicación.
4. **Errores.** Códigos del `contracts/error-catalog.md`; Problem Details con `code` y `traceId`. Los comportamientos que el legado resolvía en silencio y la ficha marca como rareza se **preservan** y quedan explícitos en la respuesta.
5. **Vecinos.** Puerto de salida + adaptador HTTP anticorrupción hacia `NEIGHBOR_BASE_URL` (por defecto `http://localhost:8090`); error del vecino → 503. En las pruebas, dobles del puerto.
6. **Pruebas.** Por cada `BR-`: prueba de dominio parametrizada con los **casos E2 de `01-recorte.md`** (típico y límite), citando la BR. Una prueba de capa web por operación contra el contrato (actualiza el OpenAPI usado por `ContractTest` con `contracts/<dominio_en>.openapi.yaml`). **ArchUnit debe seguir pasando.** Ejecuta `./mvnw -q verify` al terminar el esqueleto y al terminar todo, no tras cada archivo.
7. **Arranque.** `SPRING_PROFILES_ACTIVE=nodocker ./mvnw spring-boot:run` (o el jar), comprueba `/actuator/health` y un `curl` feliz y uno de error de negocio con los datos de la semilla.
8. Marca cada tarea `[BE]` con `Edit` al terminarla.

## Si te pasas del tiempo

La última regla/caso de uso pasa al backlog (`01-recorte.md` §5) y se desmarca del change con una nota; lo implementado debe quedar **corriendo y verde**.

## No hagas

Informe de implementación, matriz de trazabilidad, funcionalidad sin spec, mejoras de comportamiento, cambios de versiones, procesos batch (fuera del recorte).

Cierre en ≤ 5 líneas (tareas `[BE]` hechas, pruebas verdes, endpoint probado) y siguiente: `T6_LEVANTAR_Y_VERIFICAR.md` cuando también termine T5B.
