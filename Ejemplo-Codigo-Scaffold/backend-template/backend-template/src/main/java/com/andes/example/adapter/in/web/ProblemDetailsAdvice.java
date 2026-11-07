package com.andes.example.adapter.in.web;

import com.andes.example.domain.exception.BusinessRuleViolation;
import com.andes.example.domain.exception.DomainException;
import com.andes.example.domain.exception.NotFound;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Traduce errores a RFC 9457 (application/problem+json) con {@code code} estable y {@code traceId}. */
@RestControllerAdvice
public class ProblemDetailsAdvice extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailsAdvice.class);
    private static final String TYPE_PREFIX = "https://errors.andes.com/example/";

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Object> handleDomain(DomainException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex) {
            case BusinessRuleViolation v -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NotFound n -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.CONFLICT;
        };
        return build(status, ex.code(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "EX-9999", "Error interno", request.getRequestURI());
    }

    /** Todos los errores de framework (formato, cabeceras, rutas...) pasan por aquí. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String path = request instanceof ServletWebRequest swr ? swr.getRequest().getRequestURI() : null;
        String code;
        String detail;
        if (ex instanceof MissingRequestHeaderException m && "Idempotency-Key".equalsIgnoreCase(m.getHeaderName())) {
            code = "EX-0005";
            detail = "Falta la cabecera Idempotency-Key";
        } else if (statusCode.value() == 400) {
            code = "EX-0001";
            detail = "Formato inválido de la solicitud";
        } else if (statusCode.value() == 404) {
            code = "EX-0004";
            detail = "Recurso no encontrado";
        } else {
            code = "EX-9998";
            detail = "Solicitud no soportada";
        }
        return build(HttpStatus.valueOf(statusCode.value()), code, detail, path);
    }

    private ResponseEntity<Object> build(HttpStatus status, String code, String detail, String path) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setType(URI.create(TYPE_PREFIX + code));
        if (path != null) {
            pd.setInstance(URI.create(path));
        }
        pd.setProperty("code", code);
        String traceId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (traceId != null) {
            pd.setProperty("traceId", traceId);
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(pd);
    }
}
