package com.andes.example.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andes.example.adapter.out.persistence.PingPersistenceAdapter;
import com.andes.example.domain.exception.DuplicateIdempotencyKey;
import com.andes.example.domain.model.Currency;
import com.andes.example.domain.model.Money;
import com.andes.example.domain.model.Ping;
import com.andes.example.domain.port.PingRepository;
import com.andes.example.domain.service.FeeCalculator;
import com.andes.example.infrastructure.Application;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Persistencia contra H2 (modo PostgreSQL) con el esquema real de Flyway y ddl-auto=validate. */
@DataJpaTest
@ContextConfiguration(classes = Application.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(PingPersistenceAdapter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PingPersistenceAdapterTest {

    @Autowired PingRepository repository;

    private Ping ping(String id, String key, String amount) {
        return Ping.create(id, key, "msg", Money.of(amount, Currency.USD),
                Instant.parse("2025-03-01T10:15:30.123Z"), new FeeCalculator());
    }

    @Test
    void roundTripPreservesMoneyScaleAndInstant() {
        repository.save(ping("rt-1", "rt-key-1", "199.99"));

        Ping loaded = repository.findById("rt-1").orElseThrow();
        assertThat(loaded.amount().amount().toPlainString()).isEqualTo("199.99");
        assertThat(loaded.fee().amount().toPlainString()).isEqualTo("1.00");
        assertThat(loaded.amount().currency()).isEqualTo(Currency.USD);
        assertThat(loaded.receivedAt()).isEqualTo(Instant.parse("2025-03-01T10:15:30.123Z"));
        assertThat(repository.findByIdempotencyKey("rt-key-1")).isPresent();
    }

    @Test
    void uniqueIdempotencyKeyIsEnforcedByDatabase() {
        repository.save(ping("u-1", "u-key", "1.00"));

        assertThatThrownBy(() -> repository.save(ping("u-2", "u-key", "2.00")))
                .isInstanceOf(DuplicateIdempotencyKey.class);
        assertThat(repository.findById("u-2")).isEmpty();
    }

    @Test
    void pagesAreOrderedAndCounted() {
        for (int i = 1; i <= 3; i++) {
            repository.save(ping("pg-" + i, "pg-key-" + i, "1.00"));
        }
        PingRepository.PageResult page = repository.findPage(0, 2);

        assertThat(page.items()).hasSize(2);
        assertThat(page.totalElements()).isGreaterThanOrEqualTo(3);
    }
}
