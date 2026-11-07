package com.andes.example.application.usecase;

import com.andes.example.application.dto.CreatePingCommand;
import com.andes.example.application.dto.CreatePingResult;
import com.andes.example.application.dto.PingViews;
import com.andes.example.application.port.in.CreatePing;
import com.andes.example.application.port.out.ClockPort;
import com.andes.example.application.port.out.IdGeneratorPort;
import com.andes.example.domain.exception.DuplicateIdempotencyKey;
import com.andes.example.domain.model.Currency;
import com.andes.example.domain.model.Money;
import com.andes.example.domain.model.Ping;
import com.andes.example.domain.port.PingRepository;
import com.andes.example.domain.service.FeeCalculator;
import java.util.Optional;

/**
 * Crea un ping de forma idempotente. La unidad atómica es un único INSERT (la transacción la aporta el
 * repositorio); la unicidad de la clave la garantiza la BD y la carrera se resuelve releyendo el original.
 */
public class CreatePingUseCase implements CreatePing {

    private final PingRepository repository;
    private final ClockPort clock;
    private final IdGeneratorPort ids;
    private final FeeCalculator feeCalculator;

    public CreatePingUseCase(PingRepository repository, ClockPort clock, IdGeneratorPort ids,
                             FeeCalculator feeCalculator) {
        this.repository = repository;
        this.clock = clock;
        this.ids = ids;
        this.feeCalculator = feeCalculator;
    }

    @Override
    public CreatePingResult execute(CreatePingCommand command) {
        Optional<Ping> existing = repository.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            return new CreatePingResult(PingViews.of(existing.get()), false);
        }
        Money amount = new Money(command.amount(), Currency.valueOf(command.currency()));
        Ping ping = Ping.create(ids.nextId(), command.idempotencyKey(), command.message(), amount,
                clock.now(), feeCalculator);
        try {
            return new CreatePingResult(PingViews.of(repository.save(ping)), true);
        } catch (DuplicateIdempotencyKey race) {
            Ping original = repository.findByIdempotencyKey(command.idempotencyKey()).orElseThrow(() -> race);
            return new CreatePingResult(PingViews.of(original), false);
        }
    }
}
