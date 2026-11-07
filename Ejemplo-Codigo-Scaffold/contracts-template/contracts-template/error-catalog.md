# Catálogo de errores (ejemplo del kit)

| Código | HTTP | Significado | Código legado | BR | Evidencia |
|---|---|---|---|---|---|
| EX-0001 | 400 | Formato inválido (campo faltante o tipo incorrecto) | n/a (ejemplo) | n/a | n/a |
| EX-0002 | 422 | `message` supera 140 caracteres | n/a (ejemplo) | EX-R02 | ejemplo del kit |
| EX-0003 | 422 | `amount` debe ser mayor que cero | n/a (ejemplo) | EX-R03 | ejemplo del kit |
| EX-0004 | 404 | Ping no encontrado | n/a (ejemplo) | n/a | n/a |
| EX-0005 | 400 | Falta la cabecera `Idempotency-Key` | n/a (ejemplo) | n/a | n/a |

Regla de cálculo de ejemplo **EX-R01**: `fee = amount × 0.0050`, redondeo `HALF_UP` a 2 decimales, misma moneda.
Casos de verificación de EX-R01 (calculados con `Decimal`, HALF_UP):

| Entrada | Comisión |
|---|---|
| 1000.00 PEN | 5.00 PEN |
| 1.00 PEN | 0.01 PEN |
| 0.01 PEN | 0.00 PEN |
| 199.99 USD | 1.00 USD |
| 0.10 EUR | 0.00 EUR |
| 2.50 PEN | 0.01 PEN |
