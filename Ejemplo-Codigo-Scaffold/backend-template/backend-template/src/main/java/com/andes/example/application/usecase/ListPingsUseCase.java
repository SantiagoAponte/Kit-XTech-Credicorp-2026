package com.andes.example.application.usecase;

import com.andes.example.application.dto.PingPageView;
import com.andes.example.application.dto.PingViews;
import com.andes.example.application.port.in.ListPings;
import com.andes.example.domain.port.PingRepository;
import org.springframework.transaction.annotation.Transactional;

public class ListPingsUseCase implements ListPings {

    private final PingRepository repository;

    public ListPingsUseCase(PingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public PingPageView execute(int page, int size) {
        PingRepository.PageResult result = repository.findPage(page, size);
        return new PingPageView(result.items().stream().map(PingViews::of).toList(), page, size,
                result.totalElements());
    }
}
