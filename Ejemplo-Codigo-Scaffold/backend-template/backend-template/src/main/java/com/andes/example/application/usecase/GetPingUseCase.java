package com.andes.example.application.usecase;

import com.andes.example.application.dto.PingView;
import com.andes.example.application.dto.PingViews;
import com.andes.example.application.port.in.GetPing;
import com.andes.example.domain.exception.NotFound;
import com.andes.example.domain.port.PingRepository;
import org.springframework.transaction.annotation.Transactional;

public class GetPingUseCase implements GetPing {

    private final PingRepository repository;

    public GetPingUseCase(PingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PingView execute(String id) {
        return repository.findById(id)
                .map(PingViews::of)
                .orElseThrow(() -> new NotFound("EX-0004", "Ping no encontrado: " + id));
    }
}
