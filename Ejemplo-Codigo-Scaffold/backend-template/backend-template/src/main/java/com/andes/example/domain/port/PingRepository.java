package com.andes.example.domain.port;

import com.andes.example.domain.model.Ping;
import java.util.List;
import java.util.Optional;

/** Puerto de repositorio (lo implementa el adaptador de persistencia). */
public interface PingRepository {

    /** @throws com.andes.example.domain.exception.DuplicateIdempotencyKey si la clave ya existe. */
    Ping save(Ping ping);

    Optional<Ping> findById(String id);

    Optional<Ping> findByIdempotencyKey(String idempotencyKey);

    PageResult findPage(int page, int size);

    record PageResult(List<Ping> items, long totalElements) {
    }
}
