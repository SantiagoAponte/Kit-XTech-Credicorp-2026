package com.andes.example.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PingJpaRepository extends JpaRepository<PingEntity, String> {

    Optional<PingEntity> findByIdempotencyKey(String idempotencyKey);
}
