package com.andes.example.adapter.out.persistence;

import com.andes.example.domain.exception.DuplicateIdempotencyKey;
import com.andes.example.domain.model.Ping;
import com.andes.example.domain.port.PingRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PingPersistenceAdapter implements PingRepository {

    private static final Sort ORDER = Sort.by("receivedAt", "id");

    private final PingJpaRepository jpa;

    public PingPersistenceAdapter(PingJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Ping save(Ping ping) {
        try {
            return PingMapper.toDomain(jpa.saveAndFlush(PingMapper.toEntity(ping)));
        } catch (DataIntegrityViolationException e) {
            // Unicidad garantizada en BD (uk de idempotency_key).
            throw new DuplicateIdempotencyKey(ping.idempotencyKey());
        }
    }

    @Override
    public Optional<Ping> findById(String id) {
        return jpa.findById(id).map(PingMapper::toDomain);
    }

    @Override
    public Optional<Ping> findByIdempotencyKey(String idempotencyKey) {
        return jpa.findByIdempotencyKey(idempotencyKey).map(PingMapper::toDomain);
    }

    @Override
    public PageResult findPage(int page, int size) {
        Page<PingEntity> result = jpa.findAll(PageRequest.of(page, size, ORDER));
        return new PageResult(result.getContent().stream().map(PingMapper::toDomain).toList(),
                result.getTotalElements());
    }
}
