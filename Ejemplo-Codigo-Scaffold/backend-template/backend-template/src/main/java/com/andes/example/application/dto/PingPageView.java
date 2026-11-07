package com.andes.example.application.dto;

import java.util.List;

public record PingPageView(List<PingView> items, int page, int size, long totalElements) {
}
