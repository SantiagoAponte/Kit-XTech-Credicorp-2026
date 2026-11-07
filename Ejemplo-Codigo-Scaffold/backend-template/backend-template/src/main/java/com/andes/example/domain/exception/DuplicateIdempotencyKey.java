package com.andes.example.domain.exception;

/**
 * La unicidad de la Idempotency-Key fue violada al persistir (condición de carrera).
 * El caso de uso la absorbe y devuelve el registro original; no llega al cliente.
 */
public class DuplicateIdempotencyKey extends DomainException {

    public DuplicateIdempotencyKey(String key) {
        super("EX-0006", "Idempotency-Key ya registrada: " + key);
    }
}
