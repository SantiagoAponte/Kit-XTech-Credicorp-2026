# Versiones del backend-template

Fecha de resolución: 2026-11-06.

| Componente | Versión | Fuente |
|---|---|---|
| Java (JDK de build y ejecución) | Temurin 21.0.9 | `/Library/Java/JavaVirtualMachines/temurin-21.jdk` |
| Spring Boot (`spring-boot-starter-parent`) | **3.5.16** (última GA de la línea 3.x; sin RC/milestone ni 4.x) | `https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml` |
| Maven wrapper | Maven 3.10.0 (`./mvnw`) | `mvn -N wrapper:wrapper` |
| Flyway (`flyway-core`, `flyway-database-postgresql`) | 11.7.2 (gestionada por el BOM de Boot 3.5.16) | Maven Central, `~/.m2` |
| Driver PostgreSQL | 42.7.11 (BOM de Boot) | Maven Central |
| H2 | 2.3.232 (BOM de Boot) | Maven Central |
| Hibernate ORM | 6.6.53.Final (BOM de Boot) | Maven Central |
| ArchUnit (`archunit-junit5`) | **1.5.1** (release actual) | `https://repo.maven.apache.org/maven2/com/tngtech/archunit/archunit-junit5/maven-metadata.xml` |
| Validador de contrato: `com.networknt:json-schema-validator` | **1.5.9** (última 1.x; existen 2.x/3.x con API distinta, no usadas) | `https://repo.maven.apache.org/maven2/com/networknt/json-schema-validator/maven-metadata.xml` |

## Nota sobre el validador OpenAPI 3.1
Los validadores "OpenAPI-nativos" habituales (p. ej. swagger-request-validator) no cubren bien OpenAPI 3.1. Como los
esquemas de OpenAPI 3.1 son JSON Schema 2020-12, la prueba de contrato (`ContractTest` + `OpenApiContractValidator`)
carga `example.openapi.yaml`, localiza operación/estado/media type declarados y valida el cuerpo real con networknt
(dialecto 2020-12, `format` activado, `$ref` resueltos contra el documento). Incluye una prueba de sanidad que demuestra
que el validador rechaza payloads no conformes (número en vez de string, 3 decimales, moneda fuera del enum).
