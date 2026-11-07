package com.andes.example.application.port.in;

import com.andes.example.application.dto.PingView;

public interface GetPing {
    PingView execute(String id);
}
