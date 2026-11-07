package com.andes.example.domain.exception;

/** Regla de negocio violada (HTTP 422). */
public class BusinessRuleViolation extends DomainException {

    public BusinessRuleViolation(String code, String message) {
        super(code, message);
    }
}
