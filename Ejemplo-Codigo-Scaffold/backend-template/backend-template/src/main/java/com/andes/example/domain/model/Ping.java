package com.andes.example.domain.model;

import com.andes.example.domain.exception.BusinessRuleViolation;
import com.andes.example.domain.service.FeeCalculator;
import java.time.Instant;

/** Agregado de ejemplo. Protege sus invariantes; inmutable una vez creado. */
public final class Ping {

    public static final int MAX_MESSAGE_LENGTH = 140;

    private final String id;
    private final String idempotencyKey;
    private final String message;
    private final Money amount;
    private final Money fee;
    private final Instant receivedAt;

    private Ping(String id, String idempotencyKey, String message, Money amount, Money fee, Instant receivedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.message = message;
        this.amount = amount;
        this.fee = fee;
        this.receivedAt = receivedAt;
    }

    /** Creación con validación de reglas EX-R02, EX-R03 y cálculo de comisión EX-R01. */
    public static Ping create(String id, String idempotencyKey, String message, Money amount,
                              Instant receivedAt, FeeCalculator feeCalculator) {
        if (message.length() > MAX_MESSAGE_LENGTH) { // EX-R02
            throw new BusinessRuleViolation("EX-0002", "El mensaje supera " + MAX_MESSAGE_LENGTH + " caracteres");
        }
        if (!amount.isPositive()) { // EX-R03
            throw new BusinessRuleViolation("EX-0003", "El importe debe ser mayor que cero");
        }
        return new Ping(id, idempotencyKey, message, amount, feeCalculator.feeFor(amount), receivedAt);
    }

    /** Rehidratación desde persistencia (datos ya validados). */
    public static Ping restore(String id, String idempotencyKey, String message, Money amount, Money fee,
                               Instant receivedAt) {
        return new Ping(id, idempotencyKey, message, amount, fee, receivedAt);
    }

    public String id() { return id; }
    public String idempotencyKey() { return idempotencyKey; }
    public String message() { return message; }
    public Money amount() { return amount; }
    public Money fee() { return fee; }
    public Instant receivedAt() { return receivedAt; }
}
