package com.andes.example.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andes.example.domain.exception.BusinessRuleViolation;
import com.andes.example.domain.model.Currency;
import com.andes.example.domain.model.Money;
import com.andes.example.domain.model.Ping;
import com.andes.example.domain.service.FeeCalculator;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PingTest {

    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    private Ping create(String message, String amount) {
        return Ping.create("id-1", "key-1", message, Money.of(amount, Currency.PEN), NOW, new FeeCalculator());
    }

    @Test
    void createsWithFee() {
        Ping ping = create("hola", "1000.00");
        assertThat(ping.fee().amount()).isEqualByComparingTo("5.00");
    }

    @Test
    void message140IsAccepted() {
        assertThat(create("a".repeat(140), "1.00").message()).hasSize(140);
    }

    @Test
    void message141ViolatesExR02() {
        assertThatThrownBy(() -> create("a".repeat(141), "1.00"))
                .isInstanceOfSatisfying(BusinessRuleViolation.class, e -> assertThat(e.code()).isEqualTo("EX-0002"));
    }

    @Test
    void zeroAndNegativeAmountViolateExR03() {
        for (String amount : new String[] {"0.00", "-5.00"}) {
            assertThatThrownBy(() -> create("ok", amount))
                    .isInstanceOfSatisfying(BusinessRuleViolation.class,
                            e -> assertThat(e.code()).isEqualTo("EX-0003"));
        }
    }
}
