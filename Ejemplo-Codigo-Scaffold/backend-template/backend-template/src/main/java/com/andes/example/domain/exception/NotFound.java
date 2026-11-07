package com.andes.example.domain.exception;

/** Recurso inexistente (HTTP 404). */
public class NotFound extends DomainException {

    public NotFound(String code, String message) {
        super(code, message);
    }
}
