package com.andes.example.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.andes.example.domain.model.Currency;
import com.andes.example.domain.model.Money;
import com.andes.example.domain.service.FeeCalculator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Trazas: regla EX-R01 (comisión 0.50%, HALF_UP, 2 decimales). Casos de error-catalog.md. */
class FeeCalculatorTest {

    private final FeeCalculator calculator = new FeeCalculator();

    @ParameterizedTest(name = "EX-R01: {0} {1} -> {2}")
    @CsvSource({
            "1000.00, PEN, 5.00",
            "1.00,    PEN, 0.01",
            "0.01,    PEN, 0.00",
            "199.99,  USD, 1.00",
            "0.10,    EUR, 0.00",
            "2.50,    PEN, 0.01"
    })
    void feeFollowsRuleExR01(String amount, Currency currency, String expectedFee) {
        Money fee = calculator.feeFor(Money.of(amount, currency));

        assertThat(fee.amount()).isEqualByComparingTo(expectedFee);
        assertThat(fee.amount().scale()).isEqualTo(2);
        assertThat(fee.currency()).isEqualTo(currency);
    }
}
