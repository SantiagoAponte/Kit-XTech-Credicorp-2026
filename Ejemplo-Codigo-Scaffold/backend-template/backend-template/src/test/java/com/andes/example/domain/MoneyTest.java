package com.andes.example.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andes.example.domain.model.Currency;
import com.andes.example.domain.model.Money;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void normalizesToScaleTwo() {
        assertThat(Money.of("5", Currency.PEN).amount().toPlainString()).isEqualTo("5.00");
        assertThat(Money.of("1000.5", Currency.USD).amount().toPlainString()).isEqualTo("1000.50");
    }

    @Test
    void rejectsSilentRounding() {
        assertThatThrownBy(() -> Money.of("1.005", Currency.PEN)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void positivity() {
        assertThat(Money.of("0.01", Currency.EUR).isPositive()).isTrue();
        assertThat(Money.of("0.00", Currency.EUR).isPositive()).isFalse();
        assertThat(Money.of("-1.00", Currency.EUR).isPositive()).isFalse();
    }
}
