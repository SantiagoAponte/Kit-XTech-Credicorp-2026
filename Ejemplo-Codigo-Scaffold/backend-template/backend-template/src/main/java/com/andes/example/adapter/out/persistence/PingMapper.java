package com.andes.example.adapter.out.persistence;

import com.andes.example.domain.model.Currency;
import com.andes.example.domain.model.Money;
import com.andes.example.domain.model.Ping;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Mapeo explícito dominio <-> persistencia. La moneda de la comisión es la del importe. */
final class PingMapper {

    private PingMapper() {
    }

    static PingEntity toEntity(Ping ping) {
        return new PingEntity(ping.id(), ping.idempotencyKey(), ping.message(), ping.amount().amount(),
                ping.amount().currency().name(), ping.fee().amount(),
                LocalDateTime.ofInstant(ping.receivedAt(), ZoneOffset.UTC));
    }

    static Ping toDomain(PingEntity e) {
        Currency currency = Currency.valueOf(e.getCurrency());
        return Ping.restore(e.getId(), e.getIdempotencyKey(), e.getMessage(),
                new Money(e.getAmount(), currency), new Money(e.getFee(), currency),
                e.getReceivedAt().toInstant(ZoneOffset.UTC));
    }
}
