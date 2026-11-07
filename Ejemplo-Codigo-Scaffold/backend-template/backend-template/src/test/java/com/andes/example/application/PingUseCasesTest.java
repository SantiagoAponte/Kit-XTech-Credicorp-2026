package com.andes.example.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andes.example.application.dto.CreatePingCommand;
import com.andes.example.application.dto.CreatePingResult;
import com.andes.example.application.dto.PingPageView;
import com.andes.example.application.port.out.ClockPort;
import com.andes.example.application.port.out.IdGeneratorPort;
import com.andes.example.application.usecase.CreatePingUseCase;
import com.andes.example.application.usecase.GetPingUseCase;
import com.andes.example.application.usecase.ListPingsUseCase;
import com.andes.example.domain.exception.BusinessRuleViolation;
import com.andes.example.domain.exception.DuplicateIdempotencyKey;
import com.andes.example.domain.exception.NotFound;
import com.andes.example.domain.model.Ping;
import com.andes.example.domain.port.PingRepository;
import com.andes.example.domain.service.FeeCalculator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Casos de uso con dobles de puertos (sin Spring). */
class PingUseCasesTest {

    static class InMemoryRepo implements PingRepository {
        final List<Ping> store = new ArrayList<>();
        boolean simulateRace;

        @Override
        public Ping save(Ping ping) {
            if (simulateRace) { // otro hilo insertó la misma clave entre el find y el save
                simulateRace = false;
                store.add(Ping.restore("winner", ping.idempotencyKey(), "ganador", ping.amount(), ping.fee(),
                        ping.receivedAt()));
                throw new DuplicateIdempotencyKey(ping.idempotencyKey());
            }
            store.add(ping);
            return ping;
        }

        @Override
        public Optional<Ping> findById(String id) {
            return store.stream().filter(p -> p.id().equals(id)).findFirst();
        }

        @Override
        public Optional<Ping> findByIdempotencyKey(String key) {
            return store.stream().filter(p -> p.idempotencyKey().equals(key)).findFirst();
        }

        @Override
        public PageResult findPage(int page, int size) {
            int from = Math.min(page * size, store.size());
            int to = Math.min(from + size, store.size());
            return new PageResult(store.subList(from, to), store.size());
        }
    }

    private final InMemoryRepo repo = new InMemoryRepo();
    private final AtomicInteger seq = new AtomicInteger();
    private final ClockPort clock = () -> Instant.parse("2025-06-01T12:00:00Z");
    private final IdGeneratorPort ids = () -> "id-" + seq.incrementAndGet();
    private CreatePingUseCase create;

    @BeforeEach
    void setUp() {
        create = new CreatePingUseCase(repo, clock, ids, new FeeCalculator());
    }

    private CreatePingCommand cmd(String key, String message, String amount) {
        return new CreatePingCommand(key, message, new BigDecimal(amount), "PEN");
    }

    @Test
    void createsAndComputesFee() {
        CreatePingResult r = create.execute(cmd("k1", "hola", "1000.00"));

        assertThat(r.created()).isTrue();
        assertThat(r.ping().id()).isEqualTo("id-1");
        assertThat(r.ping().fee().amount()).isEqualByComparingTo("5.00");
        assertThat(r.ping().receivedAt()).isEqualTo(clock.now());
    }

    @Test
    void sameKeyReturnsOriginalWithoutCreatingAnother() {
        CreatePingResult first = create.execute(cmd("k1", "hola", "1000.00"));
        CreatePingResult second = create.execute(cmd("k1", "otro", "5.00"));

        assertThat(second.created()).isFalse();
        assertThat(second.ping().id()).isEqualTo(first.ping().id());
        assertThat(repo.store).hasSize(1);
    }

    @Test
    void raceOnUniqueKeyReturnsWinner() {
        repo.simulateRace = true;
        CreatePingResult r = create.execute(cmd("k1", "hola", "1.00"));

        assertThat(r.created()).isFalse();
        assertThat(r.ping().id()).isEqualTo("winner");
    }

    @Test
    void businessRulesPropagate() {
        assertThatThrownBy(() -> create.execute(cmd("k1", "x".repeat(141), "1.00")))
                .isInstanceOfSatisfying(BusinessRuleViolation.class, e -> assertThat(e.code()).isEqualTo("EX-0002"));
        assertThatThrownBy(() -> create.execute(cmd("k2", "ok", "0.00")))
                .isInstanceOfSatisfying(BusinessRuleViolation.class, e -> assertThat(e.code()).isEqualTo("EX-0003"));
        assertThat(repo.store).isEmpty();
    }

    @Test
    void getReturnsOrThrowsNotFound() {
        create.execute(cmd("k1", "hola", "1.00"));
        GetPingUseCase get = new GetPingUseCase(repo);

        assertThat(get.execute("id-1").message()).isEqualTo("hola");
        assertThatThrownBy(() -> get.execute("nope"))
                .isInstanceOfSatisfying(NotFound.class, e -> assertThat(e.code()).isEqualTo("EX-0004"));
    }

    @Test
    void listPaginates() {
        for (int i = 0; i < 3; i++) {
            create.execute(cmd("k" + i, "m" + i, "1.00"));
        }
        PingPageView page = new ListPingsUseCase(repo).execute(1, 2);

        assertThat(page.items()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(2);
    }
}
