# T5B — Micro-frontend (≈ 16 min, en paralelo con T5A) · prompt 08

> **Rol:** desarrollador frontend React + TypeScript. Trabajas **solo en `frontend/`** (y `shell/` si hace falta) y marcas tareas `[FE]`. Otra sesión hace `backend/` a la vez: no la toques.
> **Entrada:** change aprobado (`tasks.md` `[FE]`), `contracts/` (cliente ya generado en T4), `modernizacion/02-diseno.md` §7, `prompts/anexos/A3`.

## No esperes al backend: usa el simulador del propio contrato

El simulador del kit sirve los ejemplos de **cualquier** OpenAPI de una carpeta. Levanta el del dominio en otro puerto y apunta el frontend a él:

```bash
CONTRACTS_DIR="$PWD/contracts" PORT=8081 node local-stack/simulator/server.mjs      # error de negocio: añadir ?simulate=<HTTP>
cd frontend && BACKEND_URL=http://localhost:8081 npm run dev                        # micro-frontend en :5174
```

Cuando T5A tenga el backend arriba (`:8080`), cambia a `BACKEND_URL=http://localhost:8080` (el valor por defecto) y vuelve a probar el flujo.

## Hazlo (con `/opsx:apply <change>` limitado a las tareas `[FE]`)

1. **Reemplaza el ejemplo `Ping`** por el dominio manteniendo las capas de A3: `domain` (tipos, `Money` sin `number`), `application` (casos de uso + puerto `…Gateway`), `infrastructure` (gateway sobre el cliente generado + mapeadores DTO↔modelo), `presentation` (páginas, hooks con TanStack Query que invocan casos de uso, formularios RHF + Zod), `bootstrap/composition-root.ts` como único sitio de ensamblaje. `presentation` nunca llama a `fetch` ni importa de `infrastructure`.
2. **Una pantalla por caso de uso** (según `02-diseno.md`): consulta/listado, formulario, resultado y estados de error. Si no hay pantalla legada para el recorte, rotúlala **«interfaz de operación, no equivalente a una pantalla legada»**. Rutas expuestas por `./routes` (relativas; el shell las monta en `RUTA_SHELL`).
3. **Errores.** Actualiza el diccionario `i18n/error-messages.ts` con **todos** los códigos del `contracts/error-catalog.md` → mensaje en español. Valida en el formulario solo lo simple (obligatoriedad, formato, rango); la regla de negocio la devuelve el backend.
4. **Importes** siempre `{ amount: "string", currency }`; formatea con `Intl.NumberFormat` solo para mostrar. Enmascara números de cuenta como indique el diseño.
5. **Pruebas (Vitest):** un caso de uso por prueba (con gateway falso) y una prueba de componente por pantalla (éxito y un error). `npm run lint` (reglas de capas) y `npm run build` deben pasar.
6. **Shell.** Con el frontend en `:5174` y el shell (`cd shell && npm ci && npm run dev`, `:5173`), comprueba que la pantalla carga bajo `RUTA_SHELL` y que la navegación la lista.
7. Marca cada tarea `[FE]` con `Edit` al terminarla.

## Si te pasas del tiempo

La pantalla de menor prioridad pasa al backlog con una nota en el change; lo entregado debe **compilar, pasar lint/pruebas y cargar en el shell**.

## No hagas

Estética de terminal, pantallas o campos sin origen en el diseño, código compartido con otros micro-frontends, cálculos de negocio en el navegador.

Cierre en ≤ 5 líneas (tareas `[FE]` hechas, lint/pruebas/build, pantalla en el shell) y siguiente: `T6_LEVANTAR_Y_VERIFICAR.md` cuando también termine T5A.
