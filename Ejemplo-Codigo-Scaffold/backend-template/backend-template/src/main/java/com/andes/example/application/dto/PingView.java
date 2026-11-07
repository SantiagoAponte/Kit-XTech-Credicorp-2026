package com.andes.example.application.dto;

import java.time.Instant;

public record PingView(String id, String message, MoneyView amount, MoneyView fee, Instant receivedAt) {
}
