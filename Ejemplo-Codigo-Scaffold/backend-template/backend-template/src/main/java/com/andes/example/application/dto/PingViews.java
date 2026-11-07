package com.andes.example.application.dto;

import com.andes.example.domain.model.Money;
import com.andes.example.domain.model.Ping;

/** Mapeo explícito dominio -> vista de aplicación. */
public final class PingViews {

    private PingViews() {
    }

    public static PingView of(Ping ping) {
        return new PingView(ping.id(), ping.message(), of(ping.amount()), of(ping.fee()), ping.receivedAt());
    }

    private static MoneyView of(Money money) {
        return new MoneyView(money.amount(), money.currency().name());
    }
}
