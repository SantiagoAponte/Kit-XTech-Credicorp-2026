package com.andes.example.application.dto;

import java.math.BigDecimal;

public record CreatePingCommand(String idempotencyKey, String message, BigDecimal amount, String currency) {
}
