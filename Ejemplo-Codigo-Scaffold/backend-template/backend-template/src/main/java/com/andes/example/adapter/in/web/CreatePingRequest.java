package com.andes.example.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Solo formato (→400). El límite de 140 caracteres es regla de negocio (→422) y vive en el dominio. */
public record CreatePingRequest(
        @NotNull @Size(min = 1) String message,
        @NotNull @Valid MoneyDto amount) {
}
