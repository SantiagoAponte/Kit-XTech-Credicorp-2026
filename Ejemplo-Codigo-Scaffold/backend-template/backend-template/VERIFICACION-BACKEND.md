# Verificación del backend-template

Fecha: 2026-11-06. Entorno: macOS arm64, Temurin 21.0.9, Maven 3.10.0 (wrapper), Spring Boot 3.5.16.

## (1) `./mvnw -q verify`

Salida con `-q`: vacía, exit code 0. Detalle (`./mvnw verify`, líneas relevantes):
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.705 s -- in com.andes.example.contract.ContractTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.165 s -- in com.andes.example.adapter.PingPersistenceAdapterTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.322 s -- in com.andes.example.adapter.PingControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 s -- in com.andes.example.application.PingUseCasesTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.036 s -- in com.andes.example.domain.FeeCalculatorTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.andes.example.domain.PingTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 s -- in com.andes.example.domain.MoneyTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.731 s -- in com.andes.example.architecture.ArchitectureTest
[INFO] Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Hallazgo durante la verificación: un número JSON (`"amount":1000.0`) en el campo string de dinero era coaccionado por Jackson a "1000.0" (violaba el contrato). Se corrigió con `StrictStringDeserializer`; ahora devuelve 400 EX-0001 (probado en `PingControllerTest`).

## (2) Arranque real con `SPRING_PROFILES_ACTIVE=nodocker` (java -jar target/app.jar)
```
### health
{"status":"UP","groups":["liveness","readiness"]}
{"status":"UP"}
### POST 201
HTTP/1.1 201 
X-Correlation-Id: 760b299c-fcfc-4d7a-9329-86fb9fffdef8
Content-Type: application/json
{"id":"58f5d331-f59e-4f53-a16f-53b2d16d8f64","message":"hola","amount":{"amount":"1000.00","currency":"PEN"},"fee":{"amount":"5.00","currency":"PEN"},"receivedAt":"2026-11-07T04:18:54.330Z"}
### POST repeat 200
HTTP/1.1 200 
{"id":"58f5d331-f59e-4f53-a16f-53b2d16d8f64","message":"hola","amount":{"amount":"1000.00","currency":"PEN"},"fee":{"amount":"5.00","currency":"PEN"},"receivedAt":"2026-11-07T04:18:54.330Z"}
### fee 2.50
{"id":"0a224aeb-e24b-4141-bcb3-1ec22fbd5fff","message":"fee","amount":{"amount":"2.50","currency":"EUR"},"fee":{"amount":"0.01","currency":"EUR"},"receivedAt":"2026-11-07T04:18:54.407Z"}
### 400 invalid
HTTP/1.1 400 
Content-Type: application/problem+json
{"type":"https://errors.andes.com/example/EX-0001","title":"Bad Request","status":400,"detail":"Formato inválido de la solicitud","instance":"/api/v1/pings","code":"EX-0001","traceId":"50ef81cd-969f-46aa-b3a4-02d34eee5ae5"}
### 400 numero float
400
### 400 no header
HTTP/1.1 400 
{"type":"https://errors.andes.com/example/EX-0005","title":"Bad Request","status":400,"detail":"Falta la cabecera Idempotency-Key","instance":"/api/v1/pings","code":"EX-0005","traceId":"4e87a2d2-0a61-4fb0-8f24-c954662fbea7"}
### 422 msg141
HTTP/1.1 422 
{"type":"https://errors.andes.com/example/EX-0002","title":"Unprocessable Entity","status":422,"detail":"El mensaje supera 140 caracteres","instance":"/api/v1/pings","code":"EX-0002","traceId":"c486e637-9fff-4894-bb9d-ccda17aeb2d2"}
### 422 amount 0.00
HTTP/1.1 422 
{"type":"https://errors.andes.com/example/EX-0003","title":"Unprocessable Entity","status":422,"detail":"El importe debe ser mayor que cero","instance":"/api/v1/pings","code":"EX-0003","traceId":"71681075-f572-4042-b67f-99a48e70a28d"}
### 404
HTTP/1.1 404 
Content-Type: application/problem+json
{"type":"https://errors.andes.com/example/EX-0004","title":"Not Found","status":404,"detail":"Ping no encontrado: nope","instance":"/api/v1/pings/nope","code":"EX-0004","traceId":"35276322-c908-42c5-a980-a338a54a44dd"}
### list page=0 size=2
{"items":[{"id":"seed-ping-0001","message":"Ping semilla uno","amount":{"amount":"1000.00","currency":"PEN"},"fee":{"amount":"5.00","currency":"PEN"},"receivedAt":"2025-01-15T10:00:00Z"},{"id":"seed-ping-0002","message":"Ping semilla dos","amount":{"amount":"250.50","currency":"USD"},"fee":{"amount":"1.25","currency":"USD"},"receivedAt":"2025-01-15T11:30:00Z"}],"page":0,"size":2,"totalElements":4}
### seed
{"id":"seed-ping-0002","message":"Ping semilla dos","amount":{"amount":"250.50","currency":"USD"},"fee":{"amount":"1.25","currency":"USD"},"receivedAt":"2025-01-15T11:30:00Z"}
### CORS preflight
HTTP/1.1 200 
Access-Control-Allow-Origin: http://localhost:5173
evil origin: 403
### lsof tras detener:

(vacio = sin procesos)
```
Proceso detenido con `kill`; `lsof -i :8080` sin filas de LISTEN y `pgrep -f target/app.jar` sin resultados (solo avisos de lsof sobre sistema de archivos).

## (3) Prueba de ruptura de arquitectura
Cambio temporal en `domain/model/Money.java`: `import org.springframework.util.Assert;` y `Assert.notNull(amount, "amount")`. Resultado de `./mvnw verify -Dtest=ArchitectureTest`:
```
Tests run: 7, Failures: 1 -- ArchitectureTest.domain_is_framework_free
Architecture Violation: Rule 'no classes that reside in a package com.andes.example.domain.. should depend on classes that reside in any package [org.springframework.., jakarta.., javax.persistence.., com.fasterxml.., org.hibernate..]' was violated (1 times):
Constructor <com.andes.example.domain.model.Money.<init>(BigDecimal, Currency)> calls method <org.springframework.util.Assert.notNull(Object, String)> in (Money.java:14)
```
Revertido el cambio: `./mvnw verify` -> Tests run: 41, Failures: 0, BUILD SUCCESS.

## Limitaciones
- Dockerfile no construido (sin motor Docker).
- Perfil `dev` (PostgreSQL) no ejecutado: no hay PostgreSQL local; las migraciones se validaron en H2 modo PostgreSQL (`flyway-database-postgresql` y driver incluidos).
