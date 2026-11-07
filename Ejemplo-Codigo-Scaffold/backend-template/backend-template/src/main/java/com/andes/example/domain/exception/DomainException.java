package com.andes.example.domain.exception;

/** Excepción de dominio con código estable del catálogo de errores (ADR-007). */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
