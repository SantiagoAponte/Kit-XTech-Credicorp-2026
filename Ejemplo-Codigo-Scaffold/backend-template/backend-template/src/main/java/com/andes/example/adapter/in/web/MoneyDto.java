package com.andes.example.adapter.in.web;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Dinero en JSON: importe como string decimal (nunca número de coma flotante). */
public record MoneyDto(
        @JsonDeserialize(using = StrictStringDeserializer.class) @NotNull @Pattern(regexp = "^-?\\d+(\\.\\d{1,2})?$") String amount,
        @NotNull @Pattern(regexp = "^(PEN|USD|EUR)$") String currency) {
}
