package com.andes.example.application.port.in;

import com.andes.example.application.dto.PingPageView;

public interface ListPings {
    PingPageView execute(int page, int size);
}
