# A3 — Convenciones de frontend: React + TypeScript, micro-frontend y Clean Architecture

> Complementa el ADR-003, 004, 008 y 009. Guía obligatoria para el prompt 08.

## 1. Idea general
El frontend del dominio es un **micro-frontend remoto** (se construye, versiona y despliega por separado). Un **shell** aportado por el kit lo carga en ejecución mediante **federación de módulos** y le da navegación, autenticación simulada de desarrollo y diseño base. El micro-frontend se organiza con **Clean Architecture**.

```
┌─────────────────────── shell (kit) ───────────────────────┐
│  router · layout · sesión dev · tema                      │
│   └── carga remota ──▶ [ micro-frontend del dominio ]     │
│                          expone: ./routes  (y ./App)      │
└────────────────────────────────────────────────────────────┘
                 │ HTTP (cliente generado del contrato)
                 ▼
          gateway / backend del dominio
```

## 2. Capas y regla de dependencia

```
presentation ──▶ application ──▶ domain
infrastructure ─────────────────▶ application / domain  (implementa puertos)
```
- **`domain`**: tipos y reglas de negocio puras del frontend (modelos, objetos de valor como `Money`, validaciones simples reutilizables). **Sin React ni fetch.**
- **`application`**: **casos de uso** (funciones o clases: `openTermDeposit`, `listDeposits`), **puertos** (interfaces como `TermDepositGateway`) y estado de la aplicación. Sin React.
- **`infrastructure`**: implementaciones de los puertos: **cliente HTTP** (generado a partir del OpenAPI), almacenamiento local, mapeadores DTO↔modelo. Sin React.
- **`presentation`**: componentes React, páginas, *hooks* que invocan casos de uso, formularios, rutas. **Nunca** llama a `fetch` ni conoce DTOs HTTP.

## 3. Estructura

```
frontend/
├── package.json  vite.config.ts  tsconfig.json  .eslintrc / eslint.config.*
├── src/
│   ├── domain/            money.ts  term-deposit.ts  rules/
│   ├── application/       use-cases/  ports/  state/
│   ├── infrastructure/    api/ (cliente generado)  gateways/  mappers/
│   ├── presentation/      pages/  components/  hooks/  routes.tsx  i18n/
│   ├── bootstrap/         composition-root.ts   (ensambla puertos ↔ adaptadores)
│   ├── remote-entry.tsx   (lo expone la federación: ./routes)
│   └── main.tsx           (arranque en modo independiente para desarrollo)
└── tests/                 unit/  integration/  e2e/ (opcional)
```

## 4. Patrones y librerías por defecto (las versiones las fija el kit)
- **React + TypeScript estricto + Vite.**
- **Estado de servidor:** TanStack Query en `presentation/hooks`, invocando casos de uso (no al cliente HTTP directamente).
- **Formularios:** React Hook Form + Zod; el esquema Zod refleja las reglas **simples** del contrato (obligatoriedad, rangos, formato); la regla de negocio autoritativa la devuelve el backend como Problem Details.
- **Composition root** único (`bootstrap/composition-root.ts`) que crea los adaptadores y los inyecta en los casos de uso (inyección por parámetros/contexto; sin singletons ocultos).
- **Internacionalización:** textos en español en un diccionario; mapeo de `code` de error → mensaje.
- **Accesibilidad:** semántica HTML, etiquetas asociadas, foco gestionado, contraste del sistema de diseño del kit.

## 5. Contrato del micro-frontend con el shell
- Expone **`./routes`** (arreglo de rutas que el shell monta bajo `/<dominio>`) y, opcionalmente, **`./App`**.
- **Dependencias compartidas como singleton**: `react`, `react-dom`, `react-router-dom` (versiones del kit; no las cambies).
- **Comunicación con el shell** solo por **propiedades** (usuario de desarrollo, tema, `correlation-id`) y **eventos del navegador** con prefijo del dominio (`andes:<dominio>:<evento>`). **Nunca** importar código de otro micro-frontend.
- **Estilos:** aislados (módulos CSS o prefijo de clase del dominio); no sobrescribir estilos globales del shell.

## 6. Dinero y números en el navegador
- Los importes viajan como `{ amount: "string decimal", currency }`; **no** convertir a `number` para calcular. Usa una librería de decimales o formatea con `Intl.NumberFormat` solo para mostrar.
- Cualquier cálculo de negocio vive en el backend; el frontend solo previsualiza si la regla está en `domain/rules` y tiene prueba que la iguale con los casos E2.

## 7. Pruebas y reglas de arquitectura
- **Unitarias (Vitest):** `domain` y `application` sin DOM.
- **Componentes (Testing Library):** `presentation` con casos de uso falsos.
- **Integración:** el micro-frontend montado en el shell contra el backend local (prompt 16).
- **Reglas de dependencia por lint** (por ejemplo `eslint-plugin-boundaries` o equivalente): `domain` no importa de ninguna otra capa; `application` solo de `domain`; `presentation` no importa `infrastructure`; ningún `import` entre micro-frontends.
- Cada prueba cita su `BR-` o escenario OpenSpec.

## 8. Checklist de revisión
- [ ] ¿`presentation` solo habla con casos de uso?
- [ ] ¿El cliente HTTP deriva del OpenAPI y está en `infrastructure`?
- [ ] ¿Los errores se muestran por `code` mapeado a mensaje en español?
- [ ] ¿Ningún importe pasa por `number`?
- [ ] ¿El micro-frontend arranca solo y también dentro del shell?
- [ ] ¿Pasan los lint de arquitectura y las pruebas?
