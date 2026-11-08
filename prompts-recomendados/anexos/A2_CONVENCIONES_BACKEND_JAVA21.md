# A2 — Convenciones de backend: Java 21 + Clean Architecture

> Complementa el ADR-002, 005, 006, 007 y 008. Úsalo como guía obligatoria al implementar el prompt 07.

## 1. Capas y regla de dependencia

```
adapter.in.web ─┐
adapter.in.job ─┤──▶ application ──▶ domain
adapter.out.* ──┘          ▲            ▲
        ▲                  │            │
        └────── implementan puertos ────┘
infrastructure (config / bootstrap) ── ensambla todo
```

- **`domain`**: entidades, agregados, objetos de valor, servicios de dominio, eventos de dominio, excepciones de dominio, **puertos de repositorio** (interfaces). **Sin** Spring, JPA, Jackson ni librerías de infraestructura.
- **`application`**: casos de uso (uno por clase), **puertos de entrada** (interfaz del caso de uso) y **de salida** (interfaces hacia persistencia, otros dominios, reloj, generador de identificadores), comandos/consultas y resultados. Aquí vive la **transacción**.
- **`adapter.in.web`**: controladores REST, DTOs de entrada/salida, validación de bordes, traducción de errores a Problem Details. **`adapter.in.job`**: disparadores de procesos batch.
- **`adapter.out.persistence`**: entidades JPA, repositorios Spring Data, mapeadores dominio↔persistencia, implementan los puertos de repositorio. **`adapter.out.<vecino>`**: clientes HTTP a otros dominios (anticorrupción).
- **`infrastructure`**: configuración, ensamblaje de dependencias, perfiles, arranque.

## 2. Estructura de paquetes (ejemplo para el dominio `term-deposit`)

```
backend/src/main/java/com/andes/termdeposit/
├── domain/
│   ├── model/            TermDeposit (agregado), Money, Rate, Term (records), DepositStatus (enum)
│   ├── service/          PenaltyCalculator (servicio de dominio puro)
│   ├── event/            TermDepositOpened ...
│   ├── exception/        DomainException, BusinessRuleViolation(code)
│   └── port/             TermDepositRepository (interfaz)
├── application/
│   ├── usecase/          OpenTermDepositUseCase, CancelEarlyUseCase ...
│   ├── port/in/          OpenTermDeposit (interfaz) ...
│   ├── port/out/         ClockPort, AccountPostingPort, NotificationPort
│   └── dto/              OpenTermDepositCommand, TermDepositView
├── adapter/
│   ├── in/web/           TermDepositController, ProblemDetailsAdvice, request/response DTOs
│   ├── in/job/           InterestAccrualJob
│   └── out/persistence/  TermDepositEntity, TermDepositJpaRepository, TermDepositPersistenceAdapter, mapper
│   └── out/posting/      AccountPostingHttpAdapter (o simulador)
└── infrastructure/       BeanConfig, ClockConfig, Application
backend/src/main/resources/ application.yml, application-dev.yml, application-nodocker.yml, db/migration/V1__*.sql
backend/src/test/java/    domain/ application/ adapter/ architecture/ (ArchUnit)
```
Paquete base: `com.andes.<dominio-en-ingles>` (configurable por el kit).

## 3. Patrones de diseño que se esperan
- **Aggregate + invariantes** en el dominio; los cambios de estado pasan por métodos del agregado (no setters públicos).
- **Value Objects** como `record` con validación en el constructor compacto (`Money`, `Rate`, `Term`, `AccountNumber`).
- **Caso de uso = una clase, un método público** (`execute`). Orquesta: carga agregados vía puerto, invoca el dominio, persiste, publica eventos, devuelve resultado.
- **Puertos y adaptadores** para todo lo externo (persistencia, otros dominios, reloj, aleatoriedad). `Clock` inyectado: **nunca** `LocalDate.now()` en dominio o aplicación.
- **Anticorrupción** hacia dominios vecinos: traduce el contrato externo al lenguaje del dominio.
- **Domain events** en proceso (lista de eventos en el agregado); *outbox* solo si el dominio emite eventos reales (documentado, no obligatorio en el taller).
- **Specification/Policy** para reglas con muchas condiciones (p. ej. exoneraciones por producto).
- **Factory** para creación con invariantes complejos; **Strategy** para variantes de cálculo por tipo (tarifas por tramo, productos).
- **Idempotencia**: operaciones de creación o movimiento de dinero reciben `Idempotency-Key` y la persistencia garantiza unicidad.

## 4. Reglas numéricas (ADR-006)
- `BigDecimal` siempre; escala y `RoundingMode` **declarados junto a la regla** (constante con comentario `// BR-xxx-nnn WXI-…`).
- Reproduce el **orden de operaciones** del legado, tal como lo documenta la ficha `BR-`: `a * b / c` no equivale a `a * (b / c)` con redondeos intermedios.
- Comparaciones con `compareTo`, nunca `equals` para importes.
- Nunca construir `BigDecimal` desde `double`; usar `String` o `valueOf` de enteros.

## 5. Errores y validación (ADR-007)
- `DomainException` abstracta con `code` (p. ej. `TD-0007`) y mensaje técnico; subclases por categoría (`BusinessRuleViolation`, `NotFound`, `Conflict`).
- `ProblemDetailsAdvice` (`@RestControllerAdvice`) mapea a RFC 9457: `type`, `title`, `status`, `detail`, `code`, `instance`, `traceId`.
- Validación de formato en DTOs (Bean Validation) → `400`; violación de regla de negocio → `422`; conflicto/idempotencia → `409`; inexistente → `404`.

## 6. Persistencia (ADR-005)
- Migraciones **Flyway** (`V1__create_schema.sql`, `V2__seed_*.sql` solo en perfil `dev`); nunca `ddl-auto=create` en el kit.
- Entidades JPA **distintas** del dominio; mapeadores explícitos; tipos `NUMERIC(19,2)` o la escala de la regla; `DATE` para fechas de negocio.
- Bloqueo optimista (`@Version`) donde haya concurrencia sobre un agregado.
- Nombre de tablas con prefijo del dominio (`td_deposit`) para trazar con el legado (mapeo documentado en la fase 09).

## 7. Calidad
- **Pruebas de arquitectura (ArchUnit)** mínimas: `domain` no depende de `application`, `adapter`, `infrastructure` ni de `org.springframework..`/`jakarta.persistence..`; `application` no depende de `adapter`; los adaptadores no se referencian entre sí; los controladores solo dependen de puertos de entrada.
- **Pruebas por capa:** dominio (puras, rápidas, con los casos E2), aplicación (dobles de puertos), adaptador web (capa web), persistencia (base embebida o contenedor), **contrato** (respuestas válidas contra el OpenAPI).
- Cada regla `BR-`: ≥1 prueba con valores E2 y comentario de traza.
- Logs: sin datos sensibles; `correlation-id` en cada línea (MDC).

## 8. Checklist de revisión (antes de dar por buena una fase)
- [ ] ¿El dominio compila sin dependencias de framework?
- [ ] ¿Cada caso de uso es una clase con un solo método público?
- [ ] ¿Toda regla tiene constante/comentario de traza y prueba E2?
- [ ] ¿Importes en `BigDecimal` con escala y redondeo explícitos?
- [ ] ¿Los errores devuelven Problem Details con `code` del catálogo?
- [ ] ¿Las pruebas de arquitectura pasan?
- [ ] ¿El contrato OpenAPI refleja lo implementado?
