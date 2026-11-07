package com.andes.example.infrastructure;

import com.andes.example.application.port.out.ClockPort;
import com.andes.example.application.port.out.IdGeneratorPort;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Truncado a milisegundos: la hora recibida es idéntica antes y después de persistir. */
    @Bean
    ClockPort clockPort(Clock clock) {
        return () -> Instant.now(clock).truncatedTo(ChronoUnit.MILLIS);
    }

    @Bean
    IdGeneratorPort idGeneratorPort() {
        return () -> UUID.randomUUID().toString();
    }
}
