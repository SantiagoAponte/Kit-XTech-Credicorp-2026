package com.andes.example.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Objeto de valor inmutable. Escala fija 2; nunca se construye desde double. */
public record Money(BigDecimal amount, Currency currency) {

    public static final int SCALE = 2;

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        // UNNECESSARY: un importe con más de 2 decimales significativos es un error de programación,
        // el redondeo solo ocurre de forma explícita en las reglas (p. ej. FeeCalculator).
        amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }
}
