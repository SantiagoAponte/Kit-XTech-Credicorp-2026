package com.andes.example.infrastructure;

import com.andes.example.application.port.in.CreatePing;
import com.andes.example.application.port.in.GetPing;
import com.andes.example.application.port.in.ListPings;
import com.andes.example.application.port.out.ClockPort;
import com.andes.example.application.port.out.IdGeneratorPort;
import com.andes.example.application.usecase.CreatePingUseCase;
import com.andes.example.application.usecase.GetPingUseCase;
import com.andes.example.application.usecase.ListPingsUseCase;
import com.andes.example.domain.port.PingRepository;
import com.andes.example.domain.service.FeeCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Ensamblaje manual: dominio y aplicación no conocen Spring (salvo @Transactional en consultas). */
@Configuration
public class BeanConfig {

    @Bean
    FeeCalculator feeCalculator() {
        return new FeeCalculator();
    }

    @Bean
    CreatePing createPing(PingRepository repository, ClockPort clock, IdGeneratorPort ids, FeeCalculator fees) {
        return new CreatePingUseCase(repository, clock, ids, fees);
    }

    @Bean
    GetPing getPing(PingRepository repository) {
        return new GetPingUseCase(repository);
    }

    @Bean
    ListPings listPings(PingRepository repository) {
        return new ListPingsUseCase(repository);
    }
}
