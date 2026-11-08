# Prompt 16 — Ejecución local y verificación del dominio construido

> **Rol del agente:** Ingeniero de plataforma / calidad (levanta, ejecuta y verifica de verdad)  
> **Depende de:** Fases 07 y 09 (y 08 si hay interfaz); ADR de contenerización y topología local aceptados. 10 es opcional en el camino corto del taller. **Orden recomendado: después de 07–09 (y 10 si se hizo) y antes de 11**  
> **Produce:** Entorno local reproducible (comandos, composición, datos semilla) que levanta backend, frontend y componentes de apoyo del dominio piloto, con evidencia de que funciona

## Reglas innegociables

1. **No se dispone del código COBOL; la fuente de verdad son los artefactos de Wynxx (`wynxx/`).** No busques, no reconstruyas de memoria y no modifiques código COBOL. Si falta un detalle que Wynxx no da, regístralo como vacío.
2. **No inventes.** Ni reglas de negocio, ni funcionalidades, ni tecnologías, ni comandos o estructuras de OpenSpec. Si no hay evidencia: registra un vacío `GAP-nnn` en `modernizacion/01-analisis/vacios-y-contradicciones.md`, indica cómo resolverlo y no avances sobre ese punto.
3. **Todo hecho lleva evidencia**: artefacto Wynxx (`wynxx/<archivo>#<sección>`) y, cuando ese artefacto cite el código, la referencia `COB:<archivo>:<línea o párrafo>` **tal como la cita Wynxx** (no se verifica leyendo el COBOL). Un hecho sin evidencia es un supuesto y debe etiquetarse como tal.
4. **Un cambio de comportamiento es una desviación** `DESV-nnn` en `modernizacion/aprobaciones.md`, con justificación funcional. No se implementa sin aprobación humana explícita.
5. **Las decisiones de arquitectura y tecnología son ADR** (`Proposed` → `Accepted`). Los **ADR comunes del taller** (`prompts/anexos/A1_ADR_COMUNES.md`) ya están aceptados: no se re-deciden; si la evidencia los tensiona, propón una excepción `EXC-nnn` y espera. Cuando una fase marque *PUNTO DE APROBACIÓN*, DETENTE y espera la respuesta humana; no la des por hecha.
6. **Equivalencia funcional con evidencia graduada** (nunca declares equivalencia completa sin nivel E3):
   `E0` declarado en un solo artefacto de Wynxx · `E1` corroborado: coincide en ≥2 artefactos o secciones de Wynxx, o el artefacto cita el fragmento o la ubicación del código que lo respalda · `E2` caso de prueba derivado de la regla documentada (cálculo manual o script) sin ejecutar el legado · `E3` contrastado contra ejecución real del legado o un dataset de regresión aportado.
7. **Modernizar no es traducir.** No copies la estructura de párrafos COBOL; reconstruye el comportamiento (reglas, cálculos, estados, errores de negocio) sobre una estructura moderna.
8. **OpenSpec real, no imaginado.** Antes de usar un comando, comprueba que existe con `openspec --help` / `openspec <comando> --help`.
9. **Cierra la fase** actualizando `modernizacion/ESTADO.md` (protocolo al final de este prompt).

## Verificación previa

Antes de empezar, lee `modernizacion/ESTADO.md` (si no existe y esta no es la fase 01, DETENTE: faltan fases previas). Verifica que las fases de las que depende este prompt figuren como **Aprobada**. Si alguna no lo está, no continúes: informa qué falta y por qué. Si existe `modernizacion/PARAMETROS.md` (dominio asignado, recorte, rutas, puertos), léelo y respétalo; si no existe y esta es la fase 01, créalo con el equipo (plantilla en `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md`).

**Reanudación:** si ya existen entregables de esta fase (revisa las rutas de la sección *Entregables*), léelos y continúa o actualízalos; no los rehagas ni los sobrescribas sin decirlo, y anota en `ESTADO.md` qué reutilizaste.

## Contexto

Hasta aquí hay specs, código y pruebas, pero falta **ver el dominio funcionando**: levantar de extremo a extremo los componentes construidos (servicios, bases de datos, mensajería si la hay, gateway, shell y micro-frontends o interfaz, dobles de dependencias ausentes) en la máquina local y comprobar los escenarios de las specs contra el sistema en ejecución. Esta fase es también el puente hacia el despliegue (fase 14): los artefactos de contenerización que crees aquí se reutilizan allí.

## Modo taller (dominio acotado)

**Meta visible del taller:** ver **el backend Java corriendo y el frontend React corriendo**, funcionando para el recorte del dominio, contra una base de datos pequeña en contenedor, en `localhost`. Levanta **ambos repositorios** (`backend/` y `frontend/`) con `local-stack/` del kit; el shell carga el micro-frontend. Los dominios vecinos son **opcionales** (simulador en el puerto 8090, prioridad baja). **Antes de levantar nada, detecta las herramientas instaladas y pregunta al participante cómo quiere ejecutarlo** (directo sin Docker · mixto con solo la base de datos en contenedor · Docker completo), indicando cuáles son viables y cuál recomiendas; registra la respuesta en `PARAMETROS.md` y respétala. Si el modo elegido falla por el entorno, ofrece cambiar. En el camino corto, la fase 10 no es prerrequisito. Al final entrega las URLs y un **recorrido de demostración** paso a paso.

## Objetivo

Dejar un **entorno local de un solo comando** (arranque, parada y reinicio limpio) que levante el trozo de dominio construido, cargue datos semilla deterministas, pase *smoke tests* y permita ejecutar los escenarios clave (API e interfaz) con evidencia real de ejecución, documentado para que cualquier participante lo reproduzca desde un clon limpio.

## Entradas

- `modernizacion/03-arquitectura/topologia-local-de-referencia.md`, `contenerizacion-y-nube.md` y los ADR aceptados (contenerización, estructura de código, persistencia, mensajería).
- Código y pruebas de las fases 07, 08, 09 y 10; contratos (06); modelo y migraciones de datos (09).
- Specs del change (`openspec/changes/<change>`), en especial escenarios de extremo a extremo.
- Casos E2 de las fases 02/11 para datos semilla y verificación.
- `prompts/anexos/A5_KIT_LOCAL_Y_RUTA.md` y `local-stack/` (copia del kit).

## Instrucciones

1. **Detecta lo que hay instalado.** Ejecuta los comandos de versión de las herramientas (JDK, Node.js, motor de contenedores y su herramienta de composición) y comprueba que el motor de contenedores responde (no basta con que el comando exista). **No instales nada sin permiso**: si falta algo, infórmalo.
2. **Pregunta al participante cómo quiere levantarlo y espera su respuesta** (usa la herramienta de preguntas interactivas del agente, con opciones). Presenta estas tres, indicando cuáles son **viables** según la detección y cuál recomiendas: **(1) Directo** — backend con JDK 21 y Maven, frontend con Node, base de datos H2 embebida (perfil `nodocker`), sin Docker; **(2) Mixto** — solo la base de datos PostgreSQL en contenedor, backend y frontend ejecutados directamente (reinicios rápidos; recomendada si Docker funciona); **(3) Docker completo** — todo en contenedores con la composición `full`. Registra la elección en `modernizacion/PARAMETROS.md` (`MODO_EJECUCION: directo | mixto | docker`). El participante puede **cambiar de modo después** sin rehacer el resto; si el modo elegido falla por el entorno, ofrece pasar a otro.
3. **Inventario de componentes** del dominio piloto (de la topología local): servicios, bases de datos, bróker, gateway/BFF, shell y micro-frontends (o frontend único), dobles/simuladores de dependencias ausentes y de otros contextos aún no construidos. Para cada uno: imagen o proceso, puerto, dependencias, variables de configuración, *healthcheck*.
4. **Preparación según el modo elegido.** Modo `docker`: obligatoria; modo `mixto`: solo la base de datos; modo `directo`: omite este paso salvo que el participante pida también Docker. Crea (según el ADR) los archivos de construcción de cada unidad (multietapa, usuario no root, sin secretos) y la **composición local** que las orquesta con redes, volúmenes, orden de arranque (`depende de` + esperas de salud) y perfiles (p. ej. solo-backend / todo). Ubica los archivos donde fije el ADR; si no lo fija, propón la ubicación y pide confirmación.
5. **Configuración local.** Provee un archivo de ejemplo de variables de entorno (`.env.example`) **sin secretos reales**; valores por defecto seguros para desarrollo; puertos expuestos solo en `localhost`.
6. **Datos.** Aplica migraciones/esquemas de la fase 09 al arrancar; carga **datos semilla deterministas** (sin datos reales) que ejerciten las reglas, incluidos los casos límite E2; procedimiento de reseteo a estado limpio.
7. **Scripts de operación** (agnósticos del sistema operativo cuando sea posible; `levantar` acepta el modo: `levantar directo|mixto|docker`): `levantar`, `detener`, `reiniciar-limpio`, `estado`, `logs`, `cargar-datos`, `verificar`. Documenta cada uno en el README local.
8. **Smoke tests.** Tras el arranque: salud de cada componente, conectividad entre ellos, una operación mínima por servicio, y que el shell/frontend carga y alcanza el gateway.
9. **Verificación funcional en ejecución.** Ejecuta contra el sistema levantado los escenarios WHEN/THEN seleccionados de las specs (flujo feliz, validaciones, errores de negocio, límites): por API y, si hay interfaz, por la interfaz (automatizado cuando sea posible). Incluye al menos un **proceso extremo a extremo** que cruce varios contextos y, si aplica, una ejecución del **proceso batch** equivalente con sus totales de control.
10. **Evidencia real.** Guarda resultados reales (salidas de comandos, respuestas, capturas o trazas, logs relevantes) en `modernizacion/16-ejecucion-local/evidencias/`. **No simules ni inventes resultados**: si algo falla, repórtalo con la causa.
11. **Reproducibilidad.** Demuestra el arranque desde cero (sin caché ni estado previo) y un reinicio limpio. Mide y anota el tiempo y recursos aproximados.
12. **Guía de solución de problemas** (puertos ocupados, dependencias, datos corruptos, reinicio) y limpieza completa (`detener` + borrado de volúmenes).

## Uso de OpenSpec

- Añade al `tasks.md` del change las tareas de ejecución local (contenerización, composición, datos semilla, scripts, smoke tests, verificación) con `/opsx:update <change>`; márcalas al completarlas con `/opsx:apply`.
- Si la verificación en ejecución revela una discrepancia con una spec, **no la ocultes**: decide con evidencia si el defecto está en el código o en la spec; si es la spec, actualiza con `/opsx:update` y re-valida (`openspec validate <change> --strict`).
- No archives el change: la validación de equivalencia (fases 11 y 12) todavía no ha concluido.

## Entregables

- Archivos de contenerización y composición local en la ubicación fijada por ADR.
- `.env.example`, datos semilla y scripts de operación.
- `modernizacion/16-ejecucion-local/README-local.md`: modo elegido y cómo cambiarlo, requisitos, comandos, puertos, usuarios de prueba, flujos de demostración y limpieza.
- `modernizacion/16-ejecucion-local/smoke-tests.md` y `verificacion-escenarios.md` (escenario → resultado real).
- `modernizacion/16-ejecucion-local/evidencias/` (salidas reales).
- `modernizacion/16-ejecucion-local/solucion-de-problemas.md`.

## Criterios de aceptación

- [ ] El participante **eligió el modo** de ejecución (directo, mixto o docker) tras una pregunta explícita, y quedó registrado en `PARAMETROS.md`.
- [ ] Desde un clon limpio y siguiendo solo el README local, el entorno levanta con un comando **en el modo elegido** y todos los *healthchecks* pasan.
- [ ] Cambiar de modo (directo ↔ mixto ↔ docker) funciona siguiendo el README local, sin modificar código.
- [ ] Los smoke tests pasan y los escenarios de spec seleccionados se ejecutan contra el sistema real con resultado documentado.
- [ ] Existe al menos un flujo extremo a extremo (interfaz o API) que cruza ≥2 contextos, con evidencia.
- [ ] No hay secretos ni datos reales en el repositorio; los puertos solo escuchan en `localhost`.
- [ ] El reinicio limpio y la parada eliminan estado y recursos sin residuos.
- [ ] Los fallos o limitaciones están reportados con su causa; nada está simulado.

## Trazabilidad

- Cada escenario verificado en ejecución se enlaza a su `Requirement`, su `BR-` y su evidencia (`evidencias/<archivo>`).
- Añade a la matriz las columnas `Verificado-local (Sí/No/Parcial)` y `Evidencia-local`.

## Restricciones

- Solo entorno local: no despliegues en nube ni en entornos compartidos, no uses credenciales reales ni datos reales.
- No instales software ni modifiques la configuración del sistema sin permiso explícito.
- No cambies comportamiento para «hacer que pase»: ante una discrepancia, aplica el procedimiento de OpenSpec descrito arriba.
- No dispones del COBOL: no lo busques ni lo reconstruyas de memoria.
- No presentes como demostrada la equivalencia con el legado: esta fase demuestra que el sistema **funciona y cumple las specs**, no que sea equivalente (eso es de las fases 11 y 12 con su nivel de evidencia).

## Cierre de la fase (protocolo de estado)

Al terminar, actualiza `modernizacion/ESTADO.md` con: fase `16` → estado (`Completada, pendiente de aprobación` o `Bloqueada`), fecha, resumen en 5 líneas, lista de entregables con ruta, vacíos/contradicciones nuevos (IDs), decisiones pendientes (IDs), nombres de los cambios OpenSpec creados o avanzados y el comando para verificarlos. Termina tu respuesta con una sección **«Para aprobar»** que liste exactamente qué debe revisar y aprobar el humano.

**Siguiente prompt:** `11_VALIDACION_REGLAS_NEGOCIO.md`
