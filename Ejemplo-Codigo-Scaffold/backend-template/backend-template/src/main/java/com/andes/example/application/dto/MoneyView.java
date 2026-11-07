package com.andes.example.application.dto;

import java.math.BigDecimal;

public record MoneyView(BigDecimal amount, String currency) {
}
