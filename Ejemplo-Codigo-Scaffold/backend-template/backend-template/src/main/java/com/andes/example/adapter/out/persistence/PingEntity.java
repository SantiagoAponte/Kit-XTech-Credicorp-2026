package com.andes.example.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entidad de persistencia (sin lógica de negocio), distinta del agregado de dominio. */
@Entity
@Table(name = "ex_ping")
public class PingEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt; // UTC

    @Version
    @Column(name = "version")
    private Long version;

    protected PingEntity() {
    }

    public PingEntity(String id, String idempotencyKey, String message, BigDecimal amount, String currency,
                      BigDecimal fee, LocalDateTime receivedAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.message = message;
        this.amount = amount;
        this.currency = currency;
        this.fee = fee;
        this.receivedAt = receivedAt;
    }

    public String getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getMessage() { return message; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public BigDecimal getFee() { return fee; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public Long getVersion() { return version; }
}
