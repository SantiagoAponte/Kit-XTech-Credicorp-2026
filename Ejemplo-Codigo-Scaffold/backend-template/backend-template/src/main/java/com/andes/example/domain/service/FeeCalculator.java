package com.andes.example.domain.service;

import com.andes.example.domain.model.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Servicio de dominio puro. Regla EX-R01. */
public final class FeeCalculator {

    // EX-R01: comisión = importe x 0.0050 (0.50%), HALF_UP a 2 decimales, misma moneda.
    public static final BigDecimal FEE_RATE = new BigDecimal("0.0050");
    public static final int FEE_SCALE = 2;
    public static final RoundingMode FEE_ROUNDING = RoundingMode.HALF_UP;

    public Money feeFor(Money amount) {
        BigDecimal fee = amount.amount().multiply(FEE_RATE).setScale(FEE_SCALE, FEE_ROUNDING);
        return new Money(fee, amount.currency());
    }
}
