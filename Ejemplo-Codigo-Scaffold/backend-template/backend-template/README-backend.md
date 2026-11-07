# backend-template

Backend de ejemplo del kit: Java 21, Spring Boot 3.5.x, Maven (wrapper), Clean Architecture. Implementa el contrato
`contracts-template/example.openapi.yaml` y el catálogo `error-catalog.md` (EX-0001..EX-0005, regla EX-R01).

## Requisitos
- JDK 21 (Temurin). `export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home` (macOS) antes de Maven.
- Para el perfil `dev`: PostgreSQL en `localhost:5432` (p. ej. el `local-stack` del kit). Para `nodocker` no hace falta nada.

## Comandos
```bash
./mvnw -q verify                      # compila, pruebas (dominio, casos de uso, web, persistencia, ArchUnit, contrato) y empaqueta
./mvnw test                           # solo pruebas

# Perfil nodocker (H2 en memoria, con migraciones y semillas)
SPRING_PROFILES_ACTIVE=nodocker java -jar target/app.jar
# o: SPRING_PROFILES_ACTIVE=nodocker ./mvnw spring-boot:run

# Perfil dev (PostgreSQL; credenciales SOLO de desarrollo, sobreescribibles)
SPRING_PROFILES_ACTIVE=dev DB_URL=jdbc:postgresql://localhost:5432/kitdb DB_USER=kit DB_PASSWORD=kit java -jar target/app.jar
```
Si no se indica perfil, el predeterminado es `dev`.

### Ejemplo con curl
```bash
curl -i -X POST localhost:8080/api/v1/pings \
  -H 'Idempotency-Key: demo-1' -H 'Content-Type: application/json' \
  -d '{"message":"hola","amount":{"amount":"1000.00","currency":"PEN"}}'   # 201, fee 5.00; repetir => 200 mismo id
curl -s "localhost:8080/api/v1/pings?page=0&size=20"
curl -s localhost:8080/api/v1/pings/seed-ping-0001
curl -s localhost:8080/actuator/health
```
Errores: 400 `EX-0001` (formato), 400 `EX-0005` (falta `Idempotency-Key`), 422 `EX-0002` (message > 140),
422 `EX-0003` (amount <= 0), 404 `EX-0004`; siempre `application/problem+json` con `code` y `traceId`
(= `X-Correlation-Id`). Cualquier otro fallo: 500 `EX-9999`; rutas/métodos no soportados usan `EX-9998` (extensiones
del ejemplo fuera del catálogo del kit).

## Docker
`docker build -t backend-template .` (multietapa, usuario no root, HEALTHCHECK). Configuración por variables de entorno:
`SPRING_PROFILES_ACTIVE`, `DB_URL`, `DB_USER`, `DB_PASSWORD`. (No construido en esta verificación: no había motor Docker.)

## Estructura (`com.andes.example`)
```
domain/          model (Ping, Money, Currency) · service (FeeCalculator) · exception · port (PingRepository)   [sin frameworks]
application/     usecase (Create/Get/ListPings) · port.in · port.out (ClockPort, IdGeneratorPort) · dto
adapter/in/web/  PingController, DTOs, ProblemDetailsAdvice (RFC 9457), CorrelationIdFilter
adapter/out/persistence/  PingEntity (JPA, separada del dominio), PingJpaRepository, PingMapper, PingPersistenceAdapter
infrastructure/  Application, BeanConfig, ClockConfig, CorsConfig (solo localhost:5173/5174)
resources/       application*.yml · db/migration/V1__create_ping.sql · db/seed/V100__seed_pings.sql (solo dev/nodocker)
test/            domain, application, adapter, architecture (ArchUnit), contract (OpenAPI)
```
Reglas ArchUnit: dominio sin dependencias de capas externas ni de Spring/Jakarta/Jackson/Hibernate; `application` no
depende de adaptadores; adaptadores `in` y `out` no se referencian; los controladores solo usan puertos de entrada y DTOs.

## Cómo reemplazar el ejemplo `Ping` por un dominio real
1. Sustituye `contracts/<dominio>.openapi.yaml` y copia el contrato a `src/test/resources/contract/`.
2. Renombra el paquete base (`com.andes.example` → `com.andes.<dominio>`) en código, pom, `scanBasePackages`/`@AutoConfigurationPackage`
   y reglas ArchUnit (`BASE`).
3. Reemplaza `Ping`/`Money`/`FeeCalculator` por tus agregados, objetos de valor y servicios de dominio; cada regla `BR-` con
   constante + comentario de traza y prueba con valores E2.
4. Un caso de uso por clase en `application.usecase` + interfaz en `port.in`; cablea en `BeanConfig`.
5. Nueva migración Flyway (`V2__...`; prefijo de tabla del dominio), entidad y mapeador propios; semillas en `db/seed/`.
6. Actualiza el catálogo de errores y `ProblemDetailsAdvice` solo si agregas categorías de excepción.
7. Ajusta `ContractTest` a las nuevas operaciones.

## Mapa de traza de la regla EX-R01 (comisión 0.50%, HALF_UP, 2 decimales)
| Elemento | Ubicación |
|---|---|
| Constantes `FEE_RATE=0.0050`, `FEE_SCALE=2`, `FEE_ROUNDING=HALF_UP` | `domain/service/FeeCalculator.java` |
| Aplicación al crear el agregado | `domain/model/Ping.create(...)` |
| Orquestación | `application/usecase/CreatePingUseCase` |
| Persistencia de `fee` | columna `ex_ping.fee NUMERIC(19,2)` (`V1__create_ping.sql`) |
| Exposición | campo `fee` (`Money`) de `Ping` en el contrato OpenAPI |
| Pruebas con los 6 casos de la tabla | `domain/FeeCalculatorTest`, `PingTest`; contrato en `contract/ContractTest` |
| Seeds | `V100__seed_pings.sql` (1000.00→5.00, 250.50→1.25) |
