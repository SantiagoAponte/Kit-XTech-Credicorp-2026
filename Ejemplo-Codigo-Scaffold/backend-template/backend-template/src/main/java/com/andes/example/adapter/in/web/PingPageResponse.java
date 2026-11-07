package com.andes.example.adapter.in.web;

import com.andes.example.application.dto.PingPageView;
import java.util.List;

public record PingPageResponse(List<PingResponse> items, int page, int size, long totalElements) {

    static PingPageResponse from(PingPageView v) {
        return new PingPageResponse(v.items().stream().map(PingResponse::from).toList(), v.page(), v.size(),
                v.totalElements());
    }
}
