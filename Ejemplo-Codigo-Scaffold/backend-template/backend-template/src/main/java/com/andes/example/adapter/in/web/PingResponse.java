package com.andes.example.adapter.in.web;

import com.andes.example.application.dto.MoneyView;
import com.andes.example.application.dto.PingView;
import java.time.Instant;

public record PingResponse(String id, String message, MoneyDto amount, MoneyDto fee, Instant receivedAt) {

    static PingResponse from(PingView v) {
        return new PingResponse(v.id(), v.message(), money(v.amount()), money(v.fee()), v.receivedAt());
    }

    private static MoneyDto money(MoneyView m) {
        return new MoneyDto(m.amount().toPlainString(), m.currency());
    }
}
