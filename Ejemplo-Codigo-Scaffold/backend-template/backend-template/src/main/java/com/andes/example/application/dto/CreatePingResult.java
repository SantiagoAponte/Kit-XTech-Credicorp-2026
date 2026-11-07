package com.andes.example.application.dto;

/** {@code created=false} indica repetición idempotente (HTTP 200 en lugar de 201). */
public record CreatePingResult(PingView ping, boolean created) {
}
